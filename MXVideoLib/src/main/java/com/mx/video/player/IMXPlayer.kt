package com.mx.video.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.TextureView
import com.mx.video.MXVideo
import com.mx.video.beans.MXPlaySource
import com.mx.video.views.MXTextureView
import java.util.concurrent.atomic.AtomicBoolean

abstract class IMXPlayer : TextureView.SurfaceTextureListener {
    protected var mSurfaceTexture: SurfaceTexture? = null
    protected var mTextureView: MXTextureView? = null

    private var mMxVideo: MXVideo? = null
    private val isActive = AtomicBoolean(false)

    /**
     * 播放器是否可用
     */
    fun isActive() = isActive.get()
    private var isBuffering = false
    private var isPrepared = false
    private var isStartPlay = false

    protected val context: Context?
        get() = mMxVideo?.context

    private var mHandler: Handler? = null // 主线程Handler
    private var mThreadHandler: Handler? = null // 异步线程Handler
    private var threadHandler: HandlerThread? = null

    /**
     * 初始化主线程Handler和Thread进程
     */
    protected fun initHandler() {
        quitHandler()
        mHandler = Handler(Looper.getMainLooper())
        val thread = HandlerThread("IMXPlayer")
        thread.start()
        mThreadHandler = Handler(thread.looper)
        threadHandler = thread
    }

    /**
     * 取消所有运行方法，包含主线程和Thread线程
     */
    protected fun quitHandler() {
        try {
            val th = threadHandler
            val mh = mHandler
            val mth = mThreadHandler
            threadHandler = null
            mHandler = null
            mThreadHandler = null

            th?.quit()
            mh?.removeCallbacksAndMessages(null)
            mth?.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
        }
    }

    /**
     * 在主线程中运行
     * @param run 运行回调
     */
    fun postInMainThread(run: () -> Unit) {
        if (!isActive.get()) return
        val handler = mHandler ?: return

        // 包装一下，抓异常
        val catchRun = {
            try {
                run.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            catchRun.invoke()
        } else {
            handler.post(catchRun)
        }
    }

    /**
     * 在Thread中运行
     * @param run 运行回调
     */
    fun postInThread(run: () -> Unit) {
        mThreadHandler?.post {
            // 包装一下，抓异常
            try {
                run.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setMXVideo(video: MXVideo, textureView: MXTextureView) {
        mMxVideo = video
        mTextureView = textureView
        isActive.set(true)

        isBuffering = false
        isPrepared = false
        isStartPlay = false
    }

    /**
     * 设置播放源
     */
    abstract fun setSource(source: MXPlaySource)

    /**
     * 开始加载视频
     */
    abstract fun prepare()

    /**
     * 当播放器prepare后调用，开始播放
     */
    abstract fun start()

    /**
     * 暂停
     */
    abstract fun pause()

    /**
     * 是否正在播放中
     */
    abstract fun isPlaying(): Boolean

    /**
     * 快进，单位：秒
     */
    abstract fun seekTo(time: Int)

    /**
     * 释放资源
     */
    open fun release() {
        isActive.set(false)
        mMxVideo = null
        mTextureView = null
        Runtime.getRuntime().gc()
    }

    /**
     * 获取当前播放时间，单位：秒
     */
    abstract fun getCurrentPosition(): Int

    /**
     * 返回播放总时长，单位：秒
     */
    abstract fun getDuration(): Int

    /**
     * 设置播放器音量
     * 取值范围：0.0 -> 1.0
     */
    abstract fun setVolume(leftVolume: Float, rightVolume: Float)

    /**
     * 设置播放速度
     */
    abstract fun setSpeed(speed: Float)

    /**
     * 是否支持预加载
     */
    open fun enablePreload() = false

    /**
     * 播放错误
     */
    protected fun notifyError(message: String?) {
        if (!isActive.get()) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerError(message)
            release()
        }
    }

    /**
     * 视频宽高
     */
    protected fun notifyVideoSize(width: Int, height: Int) {
        if (!isActive.get()) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerVideoSizeChanged(width, height)
        }
    }

    /**
     * seek完成回调
     */
    protected fun notifySeekComplete() {
        if (!isActive.get()) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerSeekComplete()
        }
    }

    /**
     * 播放完成
     */
    protected fun notifyPlayerCompletion() {
        release()
        if (!isActive.get()) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerCompletion()
        }
    }

    /**
     * 缓冲进度更新
     */
    protected fun notifyBufferingUpdate(percent: Int) {
        if (!isActive.get()) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerBufferProgress(percent)
        }
    }

    /**
     * 缓冲状态更新
     * @param start true=正在缓冲，false=缓冲完成
     */
    protected fun notifyBuffering(start: Boolean) {
        if (!isActive.get()) return
        val oldBuffering = isBuffering
        isBuffering = start

        if (!isPrepared || !isStartPlay) return
        if (oldBuffering == start) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerBuffering(start)
        }
        isBuffering = start
    }

    /**
     * 播放器准备完成，可以调用#start()方法播放
     */
    protected fun notifyPrepared() {
        if (!isActive.get()) return
        if (isPrepared) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerPrepared()
        }
        isPrepared = true
    }

    /**
     * 播放正式开始！
     */
    protected fun notifyStartPlay() {
        if (!isActive.get()) return
        if (isStartPlay) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerStartPlay()
            if (isBuffering){
                video.onPlayerBuffering(true)
            }
        }
        isStartPlay = true
    }

    /**
     * 播放信息输出
     */
    protected fun onPlayerInfo(message: String?) {
        if (!isActive.get()) return
        val video = mMxVideo ?: return
        postInMainThread {
            video.onPlayerInfo(message)
        }
    }
}
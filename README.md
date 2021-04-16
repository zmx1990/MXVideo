# MXVideo

#### 介绍
基于饺子播放器、kotlin编写的开源播放器
最新版本：[![](https://jitpack.io/v/com.gitee.zhangmengxiong/MXVideo.svg)](https://jitpack.io/#com.gitee.zhangmengxiong/MXVideo)

#### 功能特性
- 任意播放器内核（包含IJK、阿里云等等）
- 单例播放，只能同时播放一个节目
- 0代码集成全屏功能
- 可以调节音量、屏幕亮度
- 可以注册播放状态监听回调
- 播放器高度可以根据视频高度自动调节
- 播放器支持设置宽高比，设置宽高比后，高度固定。
- 自动保存与恢复播放进度（可关闭）
- 支持循环播放、全屏时竖屏模式、可关闭快进快退功能、可关闭全屏功能、可关闭非WiFi环境下流量提醒

##### 1、通过 dependence 引入MXVideo
```
    dependencies {
	        implementation 'com.gitee.zhangmengxiong:MXVideo:1.0.0'
    }
```

##### 2、页面集成
```
        <com.mx.video.MXVideoStd
            android:id="@+id/mxVideoStd"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
```

##### 3、开始播放
```
// 设置播放占位图
Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())

mxVideoStd.setSource(MXPlaySource(Uri.parse("https://aaa.bbb.com/xxx.mp4"), "标题1"))
mxVideoStd.startPlay()

```

##### 4、监听播放进度
```
mxVideoStd.addOnVideoListener(object : MXVideoListener() {
            // 播放状态变更
            override fun onStateChange(state: MXState) {
            }

            // 播放时间变更
            override fun onPlayTicket(position: Int, duration: Int) {
            }
        })
```

##### 5、全屏返回 + 释放资源
```
    // 这里MXVideo默认持有当前播放的MXVideoStd，可以使用静态方法操作退出全屏、释放资源等功能。
    // 也可以直接使用viewId：mxVideoStd.isFullScreen()，mxVideoStd.isFullScreen()，mxVideoStd.release() 等方法。


    override fun onBackPressed() {
        if (MXVideo.isFullScreen()) {
            MXVideo.gotoNormalScreen()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        MXVideo.releaseAll()
        super.onDestroy()
    }
```

### 功能相关
- 切换播放器内核
```kotlin
// 默认MediaPlayer播放器，库默认内置
com.mx.video.player.MXSystemPlayer

// 谷歌的Exo播放器
com.mx.mxvideo_demo.player.MXExoPlayer

// IJK播放器
com.mx.mxvideo_demo.player.MXIJKPlayer

// 设置播放源是可以设置内核，默认 = MXSystemPlayer
mxVideoStd.setSource(MXPlaySource(Uri.parse("xxx"), "xxx"), clazz = MXSystemPlayer::class.java)
```

- 视频渲染旋转角度
```kotlin
// 默认旋转角度 = MXDegree.DEGREE_0
mxVideoStd.setDegree(MXDegree.DEGREE_0)
```

- 视频填充规则
```kotlin
// 强制填充宽高 MXScale.FILL_PARENT
// 根据视频大小，自适应宽高 MXScale.CENTER_CROP

// 默认填充规则 = MXScale.CENTER_CROP
mxVideoStd.setScaleType(MXScale.CENTER_CROP)
```

- MXVideoStd 控件宽高约束
```xml
    <!-- 在页面xml中添加，layout_width一般设置match_parent，高度wrap_content -->
    <com.mx.video.MXVideoStd
        android:id="@+id/mxVideoStd"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```
```kotlin
// MXVideoStd控件设置宽高比= 16：9
mxVideoStd.setDimensionRatio(16.0 / 9.0)

// MXVideoStd控件设置宽高比= 4：3
mxVideoStd.setDimensionRatio(4.0 / 3.0)

// 可以设置任意宽高比，如果设置宽高比，则控件高度需要设置android:layout_height="wrap_content"，否则不生效。
```
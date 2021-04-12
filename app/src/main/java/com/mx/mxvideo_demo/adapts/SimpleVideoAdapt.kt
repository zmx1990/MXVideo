package com.mx.mxvideo_demo.adapts

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.ldjVideos
import com.mx.mxvideo_demo.player.MXIJKPlayer
import com.mx.recycleview.base.BaseSimpleAdapt
import com.mx.recycleview.base.BaseViewHolder
import com.mx.video.MXPlaySource
import com.mx.video.MXVideoStd

class SimpleVideoAdapt : BaseSimpleAdapt<String>() {
    init {
        list.addAll(ldjVideos)
        list.shuffle()
    }

    override fun createItem(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(
            R.layout.adapt_video_item, parent, false
        )
    }

    override fun bindView(position: Int, itemView: View, record: String) {
        val mxVideoStd = itemView.findViewById<MXVideoStd>(R.id.mxVideoStd)
        Glide.with(mxVideoStd.context).load(record)
            .into(mxVideoStd.getPosterImageView())
//            mxVideoStd.setDimensionRatio(16.0 / 9.0)
        mxVideoStd.reset()
        mxVideoStd.setSource(
            MXPlaySource(Uri.parse(record), "" + position, isOnlineSource = true),
            clazz = MXIJKPlayer::class.java
        )
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        val mxVideoStd = holder.containerView.findViewById<MXVideoStd>(R.id.mxVideoStd)
        mxVideoStd?.stopPlay()
    }
}
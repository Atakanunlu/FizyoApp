package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen.videocall


import android.content.Context
import android.util.Log
import android.view.SurfaceView
import android.view.ViewGroup
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AgoraVideoCall(
    private val context: Context,
    private val appId: String = "ff4dd24b0afd4a2b85dee0dfe0c103c9",
    private val channelId: String,
    private val uid: Int = 0,
    private val role: Int = 1
) {
    private val TAG = "AgoraVideoCall"

    private var rtcEngine: RtcEngine? = null
    private var localVideoContainer: ViewGroup? = null
    private var remoteVideoContainer: ViewGroup? = null

    private val _callState = MutableStateFlow(VideoCallState())
    val callState: StateFlow<VideoCallState> = _callState.asStateFlow()

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "onJoinChannelSuccess: $channel, $uid")
            _callState.value = _callState.value.copy(
                isConnected = true,
                localUid = uid
            )
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "onUserJoined: $uid")
            _callState.value = _callState.value.copy(
                remoteUid = uid,
                isRemoteUserJoined = true
            )
            setupRemoteVideo(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "onUserOffline: $uid, reason: $reason")
            _callState.value = _callState.value.copy(
                remoteUid = 0,
                isRemoteUserJoined = false
            )
            remoteVideoContainer?.removeAllViews()
        }

        override fun onError(err: Int) {
            Log.e(TAG, "onError: $err")
            _callState.value = _callState.value.copy(error = "Error code: $err")
        }
    }

    fun initialize() {
        try {
            rtcEngine = RtcEngine.create(context, appId, rtcEventHandler)
            rtcEngine?.enableVideo()

            val videoEncoderConfiguration = VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            )
            rtcEngine?.setVideoEncoderConfiguration(videoEncoderConfiguration)

            _callState.value = _callState.value.copy(isInitialized = true)
        } catch (e: Exception) {
            Log.e(TAG, "Initialize failed: ${e.message}")
            _callState.value = _callState.value.copy(error = "Initialize failed: ${e.message}")
        }
    }

    fun setLocalVideoContainer(container: ViewGroup) {
        this.localVideoContainer = container
        setupLocalVideo()
    }

    fun setRemoteVideoContainer(container: ViewGroup) {
        this.remoteVideoContainer = container
    }

    private fun setupLocalVideo() {
        val surfaceView = SurfaceView(context)
        surfaceView.setZOrderMediaOverlay(true)
        localVideoContainer?.addView(surfaceView)

        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    private fun setupRemoteVideo(uid: Int) {
        if (remoteVideoContainer?.childCount ?: 0 > 0) {
            remoteVideoContainer?.removeAllViews()
        }

        val surfaceView = SurfaceView(context)
        remoteVideoContainer?.addView(surfaceView)
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun joinChannel() {
        if (rtcEngine == null) {
            _callState.value = _callState.value.copy(error = "RtcEngine not initialized")
            return
        }

        rtcEngine?.joinChannel(null, channelId, null, uid)
        _callState.value = _callState.value.copy(isJoining = true)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
        _callState.value = VideoCallState() // Reset state
    }

    fun toggleMute(): Boolean {
        val isMuted = !_callState.value.isMuted
        rtcEngine?.muteLocalAudioStream(isMuted)
        _callState.value = _callState.value.copy(isMuted = isMuted)
        return isMuted
    }

    fun toggleCamera(): Boolean {
        val isCameraOn = !_callState.value.isCameraOn
        rtcEngine?.enableLocalVideo(isCameraOn)
        _callState.value = _callState.value.copy(isCameraOn = isCameraOn)
        return isCameraOn
    }

    fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    fun release() {
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }
}
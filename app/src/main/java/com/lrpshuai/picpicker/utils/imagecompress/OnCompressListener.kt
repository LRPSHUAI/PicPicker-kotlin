package com.lrpshuai.picpicker.utils.imagecompress

import java.io.File

interface OnCompressListener {
    fun onStart()
    fun onSuccess(file: File)
    fun onError(e: Throwable)
}

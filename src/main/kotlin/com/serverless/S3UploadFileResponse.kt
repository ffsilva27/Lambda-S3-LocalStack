package com.serverless

data class S3UploadFileResponse (
    var message: String,
    var fileName: String,
    val input: Map<String, Any>
): Response()
package com.serverless

data class S3DownloadFileResponse(val file: String, val input: Map<String, Any>): Response()
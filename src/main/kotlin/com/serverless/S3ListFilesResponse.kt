package com.serverless

data class S3ListFilesResponse (
    val folder: String,
    val files: List<String>,
    val input: Map<String, Any>?
): Response()
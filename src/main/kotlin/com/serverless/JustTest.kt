package com.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

class JustTest(
    private var s3Client: S3Client
): RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    constructor() : this(S3Client.builder()
        .region(Region.of("us-east-1")) // Região usada pelo LocalStack
        .endpointOverride(URI.create("http://localhost:4566"))
        .credentialsProvider{ AwsBasicCredentials.create("localstack", "localstack") }
        .build()) {
    }

    override fun handleRequest(input:Map<String, Any>, context: Context):ApiGatewayResponse {
        LOG.info("received: " + input.keys.toString())
        val params = input["queryStringParameters"] as Map<*, *>
        LOG.info("Folder: " + params["folder"])
        LOG.info("File: " + params["file"])

        return ApiGatewayResponse.build {
            statusCode = 200
            objectBody = HelloResponse("Go Serverless v1.x! Your Kotlin function executed successfully!", input)
            headers = mapOf("X-Powered-By" to "AWS Lambda & serverless")
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(JustTest::class.java)
    }
}
package de.jensklingenberg.mpclient.http

@Target(AnnotationTarget.FUNCTION)
annotation class GET(val path:String)

@Target(AnnotationTarget.FUNCTION)
annotation class Headers(val value: Array<String>)

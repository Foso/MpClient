package de.jensklingenberg.mpclient.http

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class PATH(val path:String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class BODY()
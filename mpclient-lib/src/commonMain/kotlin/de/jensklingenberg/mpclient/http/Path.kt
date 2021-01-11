package de.jensklingenberg.mpclient.http

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path(val path:String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Body()

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Header(val path:String)
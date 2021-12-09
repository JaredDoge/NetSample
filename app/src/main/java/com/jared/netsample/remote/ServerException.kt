package com.jared.netsample.remote

import java.io.IOException

class ServerException(val code: String, override val message: String,val errorData:Any?=null) :
    IOException()
package com.rentit.app.base

import com.rentit.app.models.user.User

typealias UsersCompletion = (List<User>) -> Unit
typealias UserCompletion = (User) -> Unit
typealias Completion = () -> Unit
package com.bentley.auth.user

import org.springframework.modulith.ApplicationModule

@ApplicationModule(id = "auth.user", allowedDependencies = ["core", "auth"])
class UserApplicationModule
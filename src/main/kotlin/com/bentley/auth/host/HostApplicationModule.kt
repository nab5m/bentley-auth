package com.bentley.auth.host

import org.springframework.modulith.ApplicationModule

@ApplicationModule(id = "host", allowedDependencies = ["core", "auth"])
class HostApplicationModule
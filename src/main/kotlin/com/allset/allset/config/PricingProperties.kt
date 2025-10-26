package com.allset.allset.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
@ConfigurationProperties(prefix = "app.pricing")
class PricingProperties {
    var basePrice: BigDecimal = BigDecimal.ZERO
}

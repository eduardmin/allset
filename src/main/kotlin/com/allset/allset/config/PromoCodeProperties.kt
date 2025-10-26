package com.allset.allset.config

import com.allset.allset.model.DiscountType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.Instant

@Configuration
@ConfigurationProperties(prefix = "app.promo-codes")
class PromoCodeProperties {
    var codes: List<PromoCodeDefinition> = emptyList()

    class PromoCodeDefinition {
        var code: String = ""
        var discountType: DiscountType = DiscountType.PERCENTAGE
        var discountValue: BigDecimal = BigDecimal.ZERO
        var active: Boolean = true
        var startsAt: Instant? = null
        var expiresAt: Instant? = null
    }
}

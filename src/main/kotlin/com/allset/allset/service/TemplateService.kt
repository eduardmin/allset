package com.allset.allset.service

import com.allset.allset.model.InvitationModel
import com.allset.allset.model.Template
import com.allset.allset.model.TemplateType
import org.springframework.stereotype.Service

@Service
class TemplateService {

    private val defaultInvitationModel = InvitationModel(
        mail = true,
        date = true,
        title = true,
        description = true,
        mainImage = true,
        closingText = true,
        timeline = true,
        dressCode = true,
        confirmation = true,
        album = true,
        connectWithUs = true
    )

    val allTemplates: List<Template>
        get() {
            val romanticTemplate = Template(
                id = "template-romantic",
                type = TemplateType.ROMANTIC,
                name = "Romantic Style",
                templateImage = "romantic_image_url",
                templateDescription = "Pastel, flowerish elements, frames, ornaments. 5–7 photos. Wedding agenda up to 8 points.",
                invitationModel = defaultInvitationModel
            )

            val armenianChicTemplate = Template(
                id = "template-armenian-chic",
                type = TemplateType.ARMENIAN_CHIC,
                name = "Armenian Chic",
                templateImage = "armenian_chic_image_url",
                templateDescription = "Inspired by Armenian ornaments and elements, rustic but chic. 5–7 photos. Wedding agenda up to 8 points.",
                invitationModel = defaultInvitationModel
            )

            val elegantClassyTemplate = Template(
                id = "template-elegant-classy",
                type = TemplateType.ELEGANT_CLASSY,
                name = "Elegant and Classy",
                templateImage = "elegant_classy_image_url",
                templateDescription = "Elegant like a pearl necklace. 5–7 photos. Wedding agenda up to 8 points.",
                invitationModel = defaultInvitationModel
            )

            return listOf(romanticTemplate, armenianChicTemplate, elegantClassyTemplate)
        }

    fun getTemplates(): List<Template> {
        return allTemplates
    }
}

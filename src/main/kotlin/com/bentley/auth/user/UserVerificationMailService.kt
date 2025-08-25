package com.bentley.auth.user

import jakarta.mail.internet.MimeMessage
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class UserVerificationMailService(
    private val userVerificationService: UserVerificationService,
    private val javaMailSender: JavaMailSender,
) {
    fun sendVerificationEmail(user: User) {
        val verificationCode = Random.nextInt(100000, 999999).toString()
        val userVerification = UserVerification(
            userId = user.id,
            code = verificationCode,
            expiresAt = LocalDateTime.now().plusMinutes(5),
        )
        userVerificationService.create(userVerification)

        val resource = ClassPathResource("email/email-verification.html")
        val html = String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replace("\${name}", user.lastName + " " + user.firstName)
            .replace("\${verificationCode}", verificationCode)

        val mimeMessage: MimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, false, "UTF-8")
        helper.setTo(user.email)
        helper.setSubject("Bentley Auth - 이메일 인증번호입니다.")
        helper.setText(html, true)

        javaMailSender.send(mimeMessage)
    }
}
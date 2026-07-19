package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LegalDocumentViewer(
    subTitle: String,
    isDark: Boolean
) {
    val primaryGreen = Color(0xFF10B981)
    val accentGold = Color(0xFFFBBF24)
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)
    val cardBg = if (isDark) Color(0xFF1E2228) else Color.White
    val cardBorder = if (isDark) Color(0xFF2D323A) else Color(0xFFE5E7EB)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Document Header Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(primaryGreen.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (subTitle) {
                        "Privacy Policy" -> Icons.Filled.Policy
                        "Terms of Service", "Terms and Conditions" -> Icons.Filled.Description
                        "Community Guidelines" -> Icons.Filled.People
                        "Payment & Refund Policy" -> Icons.Filled.Payments
                        "Safety & Trust Policy" -> Icons.Filled.VerifiedUser
                        "Account Deletion Policy" -> Icons.Filled.DeleteForever
                        else -> Icons.Filled.Gavel
                    }
                    Icon(icon, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(24.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Last Updated: July 2026 | Effective immediately",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }
            }
        }

        // Zambian Startup Notice Banner
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = primaryGreen.copy(alpha = 0.06f)),
            border = BorderStroke(0.5.dp, primaryGreen.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Flag, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(18.dp))
                Text(
                    text = "This policy is fully compliant with the laws of the Republic of Zambia, including the Data Protection Act No. 4 of 2021, governing Invexa Limited digital marketplaces.",
                    fontSize = 11.sp,
                    color = textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Document Body
        when (subTitle) {
            "Privacy Policy" -> PrivacyPolicyContent(textPrimary, textSecondary, primaryGreen, cardBg, cardBorder)
            "Terms of Service", "Terms and Conditions" -> TermsAndConditionsContent(textPrimary, textSecondary, primaryGreen, cardBg, cardBorder)
            "Community Guidelines" -> CommunityGuidelinesContent(textPrimary, textSecondary, primaryGreen, cardBg, cardBorder)
            "Payment & Refund Policy" -> PaymentAndRefundPolicyContent(textPrimary, textSecondary, primaryGreen, cardBg, cardBorder)
            "Safety & Trust Policy" -> SafetyAndTrustPolicyContent(textPrimary, textSecondary, primaryGreen, cardBg, cardBorder, accentGold)
            "Account Deletion Policy" -> AccountDeletionPolicyContent(textPrimary, textSecondary, primaryGreen, cardBg, cardBorder)
            else -> {
                Text(
                    text = "No documents found for category: $subTitle. Please select a valid document category.",
                    fontSize = 12.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp)
                )
            }
        }

        // Standard Contact & Organization Information Footer
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Legal Administration Contact Details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "If you have any queries regarding our terms, data protection workflows, or require support, please contact the Giggz Legal desk.",
                    fontSize = 11.sp,
                    color = textSecondary
                )
                Divider(color = cardBorder)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LegalMetaRow("Operating Entity", "Invexa Limited", textPrimary, textSecondary)
                    LegalMetaRow("Registration No.", "[PACRA No. LCO-2026-INVEXA-XX]", textPrimary, textSecondary)
                    LegalMetaRow("Physical Address", "[Plot No. 1021, Great East Road, Lusaka, Zambia]", textPrimary, textSecondary)
                    LegalMetaRow("Official Email", "legal@giggz.app", textPrimary, textSecondary)
                    LegalMetaRow("Scope", "SADC Region / International Expansion Ready", textPrimary, textSecondary)
                }
            }
        }
    }
}

@Composable
private fun LegalMetaRow(label: String, value: String, textPrimary: Color, textSecondary: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
    }
}

@Composable
private fun LegalSectionCard(
    number: String,
    title: String,
    primaryColor: Color,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
            content()
        }
    }
}

@Composable
private fun BulletItem(text: String, textPrimary: Color, bulletColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = bulletColor)
        Text(text, fontSize = 11.sp, color = textPrimary, lineHeight = 16.sp)
    }
}

@Composable
fun PrivacyPolicyContent(
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    cardBg: Color,
    cardBorder: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Welcome to Giggz, operated under Invexa Limited. Your privacy is paramount. This policy clarifies what information we collect, how it secures your workspace, and your rights under regional data preservation mandates.",
            fontSize = 11.sp,
            color = textSecondary,
            lineHeight = 16.sp
        )

        LegalSectionCard("1", "Information We Collect", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("We capture several streams of user data to facilitate safe labor matchmaking:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Profile Details: Names, contact emails, mobile phone numbers, skills, work biographies, and portfolio media files.", textPrimary, primaryColor)
            BulletItem("Precise Location Data: Real-time GPS coordinate streams, enabling matching of workers with nearby pieces/gigs.", textPrimary, primaryColor)
            BulletItem("Communications: Internal chat logs, documents, and files uploaded directly to other platform participants.", textPrimary, primaryColor)
            BulletItem("Financial Transactions: Mobile money numbers (MTN, Airtel, Zamtel), reference IDs, subscription fees, and premium purchase statements.", textPrimary, primaryColor)
        }

        LegalSectionCard("2", "How Your Data is Utilized", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("Your information is processed transparently to improve Giggz services:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Connecting verified workers to employers through algorithmic nearby matching.", textPrimary, primaryColor)
            BulletItem("Processing secure Mobile Money checkout, payment confirmations, and automated invoices.", textPrimary, primaryColor)
            BulletItem("Maintaining trust through fraud detection tools and automated verification steps.", textPrimary, primaryColor)
            BulletItem("Sending push alerts, transaction notifications, and localized gig updates.", textPrimary, primaryColor)
        }

        LegalSectionCard("3", "Data Security & Infrastructure", primaryColor, cardBg, cardBorder, textPrimary) {
            Text(
                "We use commercial-grade cryptographic protection (AES-256) for static data storage and TLS layers for transit communications. While no transmission method is 100% impenetrable, Giggz employs strict access control mechanisms to guarantee that administrative personnel can only access specific database profiles in support scenarios.",
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 16.sp
            )
        }

        LegalSectionCard("4", "Third-Party Service Integrations", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("Giggz shares essential data elements with trusted external infrastructure partners:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Zambian Payment Gateways: MTN MoMo, Airtel Money, and Zamtel integrations to process cash checkouts.", textPrimary, primaryColor)
            BulletItem("Firebase & Google AI Studio: Used for server-side chat logs, analytics, and operational notification services.", textPrimary, primaryColor)
            BulletItem("Map Services: Location APIs utilized strictly for computing distance thresholds between gigs and active workers.", textPrimary, primaryColor)
        }

        LegalSectionCard("5", "User Rights & Consent Controls", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("You possess robust statutory rights regarding your private dataset:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Access: Request a clear portable copy of all information stored in our secure database systems.", textPrimary, primaryColor)
            BulletItem("Correction: Modify inaccurate biographies, certification files, or payment credentials directly.", textPrimary, primaryColor)
            BulletItem("Account Deletion: Initiate account termination to purge profile markers permanently from search screens.", textPrimary, primaryColor)
        }

        LegalSectionCard("6", "Data Retention Policy", primaryColor, cardBg, cardBorder, textPrimary) {
            Text(
                "Active profiles are maintained indefinitely to sustain continuous matching services. Upon initiating deletion, Giggz applies a 30-day secure buffer to handle outstanding payment claims or dispute assessments. Following this grace period, profile records are systematically purged, except for historical accounting, financial receipts, or legal records mandated by Zambian tax guidelines.",
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun TermsAndConditionsContent(
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    cardBg: Color,
    cardBorder: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Welcome to Giggz. By installing this applet or creating an account, you establish a binding service contract with Invexa Limited. If you do not accept these platform rules, please do not utilize our digital marketplace.",
            fontSize = 11.sp,
            color = textSecondary,
            lineHeight = 16.sp
        )

        LegalSectionCard("1", "User Representation & Registration", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("To access and engage on Giggz, users must commit to these structural requirements:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Age Constraint: You must be at least 18 years old or possess legal capacity to work in Zambia.", textPrimary, primaryColor)
            BulletItem("Accurate Profiling: Provide valid names, active contact credentials, and verifiably true skills portfolios.", textPrimary, primaryColor)
            BulletItem("Credential Security: Maintain strict confidentiality regarding your login PINs or passwords.", textPrimary, primaryColor)
        }

        LegalSectionCard("2", "Worker and Employer Responsibilities", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("The marketplace depends on honest, reciprocal execution:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Employer Duties: Provide accurate gig descriptions, exact milestone payouts, and safe working conditions.", textPrimary, primaryColor)
            BulletItem("Worker Duties: Perform all accepted tasks with appropriate skill, punctuality, and professional behavior.", textPrimary, primaryColor)
            BulletItem("Tax Obligations: Users are individually responsible for reporting any direct income taxes to the Zambia Revenue Authority (ZRA).", textPrimary, primaryColor)
        }

        LegalSectionCard("3", "Rules for Job Postings and Applications", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("To prevent abuse, job postings and candidate applications are subject to strict moderation guidelines:", fontSize = 11.sp, color = textSecondary)
            BulletItem("No Multi-Level Marketing: Giggz strictly bans pyramid schemes, upfront fee models, or network-sales recruitments.", textPrimary, primaryColor)
            BulletItem("No Illegal Activities: Jobs promoting unlawful substances, transport of unvetted assets, or adult labor are immediately flagged.", textPrimary, primaryColor)
            BulletItem("Accurate Applications: Workers must not double-book concurrent schedules, leading to delayed completions.", textPrimary, primaryColor)
        }

        LegalSectionCard("4", "Prohibited Activities & Marketplace Safety", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("The following malicious behaviors trigger permanent platform bans:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Circumvention: Systematically taking Giggz matches off-platform to avoid standard premium fees or mediation safeguards.", textPrimary, primaryColor)
            BulletItem("Ama Sampo Irregularities: Advertising counterfeit assets, stolen electronics, or toxic chemicals in our marketplace sections.", textPrimary, primaryColor)
            BulletItem("System Sabotage: Trying to bypass security gates, reverse-engineer Giggz components, or harvest user data.", textPrimary, primaryColor)
        }

        LegalSectionCard("5", "Premium Features, Subscriptions & Boosts", primaryColor, cardBg, cardBorder, textPrimary) {
            Text(
                "Giggz offers premium monthly plans, highlighted gig banners, and profile boosts. Fees are settled via Mobile Money or authorized card channels. Recurring subscriptions must be cancelled manually before the renewal date to avoid automated charges. Refund procedures for administrative service errors are handled case-by-case by support.",
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 16.sp
            )
        }

        LegalSectionCard("6", "Dispute Resolution & Liability Limits", primaryColor, cardBg, cardBorder, textPrimary) {
            Text(
                "Giggz acts solely as an interactive platform connecting workers with employers; we do not employ the contractors directly. Invexa Limited is not liable for structural field disputes, personal property damage, or incomplete gig results. If a payment dispute occurs, users can submit support tickets for voluntary in-app mediation prior to seeking external legal action in Lusaka.",
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun CommunityGuidelinesContent(
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    cardBg: Color,
    cardBorder: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Giggz is built on mutual respect, professionalism, and local community trust. Our guidelines outline standard behavior expectations. We maintain a zero-tolerance policy for exploitative behaviors.",
            fontSize = 11.sp,
            color = textSecondary,
            lineHeight = 16.sp
        )

        LegalSectionCard("1", "Respectful Communication Standards", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("Our chat and marketplace elements are spaces for professional cooperation:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Zero Discrimination: Treat all members equally, regardless of ethnicity, regional background, religion, gender, or status.", textPrimary, primaryColor)
            BulletItem("Professional Tone: Profanity, slurs, threats, and sexual remarks are strictly prohibited and immediately flagged.", textPrimary, primaryColor)
            BulletItem("Conflict Resolution: If a disagreement occurs, resolve it calmly or refer the matter to Giggz support.", textPrimary, primaryColor)
        }

        LegalSectionCard("2", "Eradication of Scams & Fraudulent Posts", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("Protecting users against financial traps is our top priority:", fontSize = 11.sp, color = textSecondary)
            BulletItem("No Advance-Fee Scams: Never require job seekers to pay upfront material fees, registration fees, or training costs.", textPrimary, primaryColor)
            BulletItem("Real Gig Posting: Only post jobs where real labor is needed and direct, fair compensation is available.", textPrimary, primaryColor)
            BulletItem("Portfolio Integrity: Only post images and reference documents of work you have personally completed.", textPrimary, primaryColor)
        }

        LegalSectionCard("3", "Rating System Integrity", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("Trust is our primary asset. Rating manipulation damages the core marketplace ecosystem:", fontSize = 11.sp, color = textSecondary)
            BulletItem("No Fake Reviews: Generating fake reviews or purchasing high scores is strictly prohibited.", textPrimary, primaryColor)
            BulletItem("No Retaliatory Strike: Giving a poor rating strictly to harm a competitor will result in an immediate profile review.", textPrimary, primaryColor)
            BulletItem("Honest Feedback: Give detailed, constructive ratings based entirely on the actual gig experience.", textPrimary, primaryColor)
        }

        LegalSectionCard("4", "Reporting & Moderation Workflows", primaryColor, cardBg, cardBorder, textPrimary) {
            Text(
                "Every listing and message has a simple 'Flag' button to report violations. Once reported, Giggz administrators review the context within 24 hours. Violators receive warnings, suspension, or permanent hardware bans depending on the severity of the offense.",
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PaymentAndRefundPolicyContent(
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    cardBg: Color,
    cardBorder: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(primaryColor.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Payments,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Payment & Refund Policies",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COMING SOON",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryColor,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "The integrated Mobile Money (MTN, Airtel, Zamtel) automated escrow payment, boost micro-transactions, and direct refund workflows are currently undergoing final regulator assessments with the Bank of Zambia.",
                    fontSize = 12.sp,
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Text(
                    text = "This policy document will be officially published and activated once standard secure transaction pathways are fully cleared.",
                    fontSize = 11.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun SafetyAndTrustPolicyContent(
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    cardBg: Color,
    cardBorder: Color,
    accentGold: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Trust is the foundation of the Giggz marketplace. This policy describes our safety features and provides recommended best practices to ensure secure, reliable working relationships.",
            fontSize = 11.sp,
            color = textSecondary,
            lineHeight = 16.sp
        )

        LegalSectionCard("1", "User Verification Systems", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("We utilize multiple layers of verification to build community trust:", fontSize = 11.sp, color = textSecondary)
            BulletItem("SMS Phone Verification: Confirms that every active profile is tied to a verified mobile phone number.", textPrimary, primaryColor)
            BulletItem("Verification Badges: Earned by completing successful gigs, passing identity reviews, or submitting business credentials.", textPrimary, primaryColor)
            BulletItem("NRC Verification (Upcoming): Providing secure, optional uploads of National Registration Cards for enhanced trust scores.", textPrimary, primaryColor)
        }

        LegalSectionCard("2", "Trust Scores & Double-Review Loop", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("Our mutual review system incentivizes outstanding conduct on the platform:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Reciprocal Reviews: Workers and employers rate each other upon completing a gig.", textPrimary, primaryColor)
            BulletItem("Trust Footprint: High ratings raise a user's visibility, while consistent low ratings trigger automatic profile audits.", textPrimary, primaryColor)
        }

        LegalSectionCard("3", "Critical Security Recommendations", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("We strongly advise users to follow these safety recommendations:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Public Settings: Meet at public, well-lit locations for initial gig discussions.", textPrimary, primaryColor)
            BulletItem("Share Locations: Notify family or friends of your work location before beginning a job.", textPrimary, primaryColor)
            BulletItem("In-App Communication: Keep all discussions inside the Giggz chat to ensure a secure record of agreement.", textPrimary, primaryColor)
        }

        LegalSectionCard("4", "Fraud Detection & Immediate Reporting", primaryColor, cardBg, cardBorder, textPrimary) {
            Text(
                "Our automated systems monitor for spam patterns, suspicious links, and sudden changes in login location. If you encounter suspicious behavior, use the 'Report User' button to notify Giggz administrators immediately. We take rapid action to protect our community.",
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AccountDeletionPolicyContent(
    textPrimary: Color,
    textSecondary: Color,
    primaryColor: Color,
    cardBg: Color,
    cardBorder: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "We believe in user control over personal data. This policy details how to close your Giggz account, what happens to your stored data, and the legal records we are required to retain.",
            fontSize = 11.sp,
            color = textSecondary,
            lineHeight = 16.sp
        )

        LegalSectionCard("1", "Self-Service Deletion Process", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("Initiating account deletion is a simple, straightforward process:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Navigate: Go to Profile Settings -> About Giggz -> Account Deletion Policy.", textPrimary, primaryColor)
            BulletItem("Select: Tap the 'Initiate Deletion' button to start the process.", textPrimary, primaryColor)
            BulletItem("Confirm: Review the final warning prompt and verify your decision.", textPrimary, primaryColor)
        }

        LegalSectionCard("2", "What Happens to Your Data", primaryColor, cardBg, cardBorder, textPrimary) {
            Text("Once confirmed, Giggz applies the following data deletion workflow:", fontSize = 11.sp, color = textSecondary)
            BulletItem("Profile Purge: Your profile is removed from search screens and public directories.", textPrimary, primaryColor)
            BulletItem("Gigs and Listings: Your open gig postings and marketplace offers are deactivated.", textPrimary, primaryColor)
            BulletItem("Chat Logs: Chat histories are hidden from your view (though they may remain visible to recipients for dispute safety).", textPrimary, primaryColor)
        }

        LegalSectionCard("3", "Compliance and Legal Retention Exceptions", primaryColor, cardBg, cardBorder, textPrimary) {
            Text(
                "In compliance with PACRA, the Bank of Zambia, and ZRA guidelines, some financial records must be retained. Giggz stores historical invoice records, tax statements, and active dispute histories for up to six years. These records are kept strictly secure and are only accessed for compliance purposes.",
                fontSize = 11.sp,
                color = textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

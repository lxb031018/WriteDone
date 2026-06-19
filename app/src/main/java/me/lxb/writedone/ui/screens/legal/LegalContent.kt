package me.lxb.writedone.ui.screens.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.res.stringResource
import me.lxb.writedone.R

@Composable
fun UserAgreementBody() {
    LRule()
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_last_updated)) }
        append(" 2026-06-08")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_effective_date)) }
        append(" 2026-06-08")
    })
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_preamble))
    })

    LRule()

    LH2(stringResource(R.string.legal_section1))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_app_desc))
    })
    LH3(stringResource(R.string.legal_section1_1))
    LBullet(stringResource(R.string.legal_core1))
    LBullet(stringResource(R.string.legal_core2))
    LBullet(stringResource(R.string.legal_core3))
    LH3(stringResource(R.string.legal_section1_2))
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_feature_no_account)) }
    })
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_feature_offline)) }; append(stringResource(R.string.legal_feature_offline_detail))
    })
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_feature_no_data)) }
    })
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_feature_no_ads)) }
    })
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_feature_free)) }
    })
    LBullet(stringResource(R.string.legal_feature_local_storage))

    LRule()

    LH2(stringResource(R.string.legal_section2))
    LH3(stringResource(R.string.legal_section2_1))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_grant))
    })
    LH3(stringResource(R.string.legal_section2_2))
    LP(stringResource(R.string.legal_prohibited_intro))
    LBullet(stringResource(R.string.legal_prohibited_reverse))
    LBullet(stringResource(R.string.legal_prohibited_commercial))
    LBullet(stringResource(R.string.legal_prohibited_derivative))
    LBullet(stringResource(R.string.legal_prohibited_marks))
    LBullet(stringResource(R.string.legal_prohibited_law))
    LBullet(stringResource(R.string.legal_prohibited_rights))

    LRule()

    LH2(stringResource(R.string.legal_section3))
    LH3(stringResource(R.string.legal_section3_1))
    LP(stringResource(R.string.legal_ownership))
    LH3(stringResource(R.string.legal_section3_2))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_access_info))
    })

    LRule()

    LH2(stringResource(R.string.legal_section4))
    LP(stringResource(R.string.legal_privacy))

    LRule()

    LH2(stringResource(R.string.legal_section5))
    LH3(stringResource(R.string.legal_section5_1))
    LP(stringResource(R.string.legal_storage))
    LBullet(stringResource(R.string.legal_storage_retain))
    LBullet(stringResource(R.string.legal_storage_uninstall))
    LH3(stringResource(R.string.legal_section5_2))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_disclaimer))
    })
    LBullet(stringResource(R.string.legal_disclaimer_data_loss))
    LBullet(stringResource(R.string.legal_disclaimer_compatibility))
    LBullet(stringResource(R.string.legal_disclaimer_misuse))
    LBullet(stringResource(R.string.legal_disclaimer_force_majeure))
    LBullet(stringResource(R.string.legal_disclaimer_third_party))
    LH3(stringResource(R.string.legal_section5_3))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_advice))
    })

    LRule()

    LH2(stringResource(R.string.legal_section6))
    LH3(stringResource(R.string.legal_section6_1))
    LP(stringResource(R.string.legal_ip_app))
    LH3(stringResource(R.string.legal_section6_3))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_feedback))
    })

    LRule()

    LH2(stringResource(R.string.legal_section7))
    LH3(stringResource(R.string.legal_section7_1))
    LP(stringResource(R.string.legal_change_right))
    LH3(stringResource(R.string.legal_section7_2))
    LP(stringResource(R.string.legal_change_notice_intro))
    LBullet(stringResource(R.string.legal_change_notice_popup))
    LBullet(stringResource(R.string.legal_change_notice_github))
    LBullet(stringResource(R.string.legal_change_notice_about))
    LH3(stringResource(R.string.legal_section7_3))
    LP(stringResource(R.string.legal_change_effectiveness))

    LRule()

    LH2(stringResource(R.string.legal_section8))
    LH3(stringResource(R.string.legal_section8_1))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_termination_by_user))
    })
    LH3(stringResource(R.string.legal_section8_2))
    LP(stringResource(R.string.legal_termination_by_us))

    LRule()

    LH2(stringResource(R.string.legal_section9))
    LH3(stringResource(R.string.legal_section9_1))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_governing_law))
    })
    LH3(stringResource(R.string.legal_section9_2))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_dispute_resolution))
    })

    LRule()

    LH2(stringResource(R.string.legal_section10))
    LP(stringResource(R.string.legal_severability))

    LRule()

    LH2(stringResource(R.string.legal_section11))
    LP(stringResource(R.string.legal_contact_intro))
    LContactLine(label = stringResource(R.string.legal_contact_email_label), value = "lxb031018@163.com")
    LContactLine(label = stringResource(R.string.legal_contact_github_label), value = "github.com/lxb031018/lishi")
    LContactLine(label = stringResource(R.string.legal_contact_project_label), value = "github.com/lxb031018/lishi")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_contact_response)) }
    })

    LRule()

    LH2(stringResource(R.string.legal_section12))
    LP(stringResource(R.string.legal_effectiveness))

    LRule()

    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_copyright)) }
    })
    LPRich(buildAnnotatedString {
        withStyle(italicSpan) { append(stringResource(R.string.legal_reference)) }
    })
}

@Composable
fun PrivacyPolicyBody() {
    LRule()
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_last_updated)) }
        append(" 2026-06-08")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_effective_date)) }
        append(" 2026-06-08")
    })
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_intro))
    })
    LP(stringResource(R.string.legal_pp_basis))

    LRule()

    LH2(stringResource(R.string.legal_pp_section1))
    LH3(stringResource(R.string.legal_pp_section1_1))
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_principle)) }
    })
    LP(stringResource(R.string.legal_pp_pii_definition))
    LH3(stringResource(R.string.legal_pp_section1_2))
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_no_collect)) }
    })
    LBullet(stringResource(R.string.legal_pp_no_identity))
    LBullet(stringResource(R.string.legal_pp_no_location))
    LBullet(stringResource(R.string.legal_pp_no_contacts))
    LBullet(stringResource(R.string.legal_pp_no_device_id))
    LBullet(stringResource(R.string.legal_pp_no_camera))
    LBullet(stringResource(R.string.legal_pp_no_sensors))
    LBullet(stringResource(R.string.legal_pp_no_logs))
    LBullet(stringResource(R.string.legal_pp_no_crash))
    LBullet(stringResource(R.string.legal_pp_no_network))
    LH3(stringResource(R.string.legal_pp_section1_3))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_audience))
    })

    LRule()

    LH2(stringResource(R.string.legal_pp_section2))
    LH3(stringResource(R.string.legal_pp_section2_1))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_storage_location))
    })
    LH3(stringResource(R.string.legal_pp_section2_2))
    LP(stringResource(R.string.legal_pp_retention_intro))
    LBullet(stringResource(R.string.legal_pp_retention_normal))
    LBullet(stringResource(R.string.legal_pp_retention_delete_item))
    LBullet(stringResource(R.string.legal_pp_retention_clear_data))
    LBullet(stringResource(R.string.legal_pp_retention_uninstall))
    LH3(stringResource(R.string.legal_pp_section2_3))
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_access_capability)) }
    })

    LRule()

    LH2(stringResource(R.string.legal_pp_section3))
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_usage_intro)) }
    })
    LP(stringResource(R.string.legal_pp_usage_detail))

    LRule()

    LH2(stringResource(R.string.legal_pp_section4))
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_sharing_intro)) }
    })
    LP(stringResource(R.string.legal_pp_sharing_commitment))
    LBullet(stringResource(R.string.legal_pp_no_share))
    LBullet(stringResource(R.string.legal_pp_no_transfer))
    LBullet(stringResource(R.string.legal_pp_no_disclose))
    LBullet(stringResource(R.string.legal_pp_no_profile))

    LRule()

    LH2(stringResource(R.string.legal_pp_section5))
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_no_sdk)) }
    })
    LBullet(stringResource(R.string.legal_pp_no_analytics_sdk))
    LBullet(stringResource(R.string.legal_pp_no_crash_sdk))
    LBullet(stringResource(R.string.legal_pp_no_ad_sdk))
    LBullet(stringResource(R.string.legal_pp_no_share_sdk))
    LBullet(stringResource(R.string.legal_pp_no_push_sdk))
    LBullet(stringResource(R.string.legal_pp_no_payment_sdk))
    LP(stringResource(R.string.legal_pp_sdk_warning))

    LRule()

    LH2(stringResource(R.string.legal_pp_section6))
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_no_permissions)) }
    })
    LP(stringResource(R.string.legal_pp_future_permissions))
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_permission_notification)) }
    })
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_no_permissions_title))
    })
    LBullet(stringResource(R.string.legal_pp_no_location_perm))
    LBullet(stringResource(R.string.legal_pp_no_camera_perm))
    LBullet(stringResource(R.string.legal_pp_no_mic_perm))
    LBullet(stringResource(R.string.legal_pp_no_contacts_perm))
    LBullet(stringResource(R.string.legal_pp_no_sms_perm))
    LBullet(stringResource(R.string.legal_pp_no_storage_perm))
    LBullet(stringResource(R.string.legal_pp_no_phone_perm))
    LBullet(stringResource(R.string.legal_pp_no_background_perm))

    LRule()

    LH2(stringResource(R.string.legal_pp_section7))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_rights_intro))
    })
    LH3(stringResource(R.string.legal_pp_section7_1))
    LP(stringResource(R.string.legal_pp_access_right))
    LH3(stringResource(R.string.legal_pp_section7_2))
    LP(stringResource(R.string.legal_pp_delete_intro))
    LBullet(stringResource(R.string.legal_pp_delete_item))
    LBullet(stringResource(R.string.legal_pp_delete_clear_data))
    LBullet(stringResource(R.string.legal_pp_delete_uninstall))
    LH3(stringResource(R.string.legal_pp_section7_3))
    LP(stringResource(R.string.legal_pp_export_right))
    LH3(stringResource(R.string.legal_pp_section7_4))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_withdraw_consent))
    })
    LH3(stringResource(R.string.legal_pp_section7_5))
    LP(stringResource(R.string.legal_pp_complaint))

    LRule()

    LH2(stringResource(R.string.legal_pp_section8))
    LH3(stringResource(R.string.legal_pp_section8_1))
    LP(stringResource(R.string.legal_pp_change_reasons))
    LBullet(stringResource(R.string.legal_pp_change_business))
    LBullet(stringResource(R.string.legal_pp_change_law))
    LBullet(stringResource(R.string.legal_pp_change_tech))
    LBullet(stringResource(R.string.legal_pp_change_feedback))
    LH3(stringResource(R.string.legal_pp_section8_2))
    LP(stringResource(R.string.legal_pp_change_methods_intro))
    LBullet(stringResource(R.string.legal_pp_change_github))
    LBullet(stringResource(R.string.legal_pp_change_about))
    LBullet(stringResource(R.string.legal_pp_change_popup))
    LH3(stringResource(R.string.legal_pp_section8_3))
    LP(stringResource(R.string.legal_pp_change_effectiveness))

    LRule()

    LH2(stringResource(R.string.legal_pp_section9))
    LP(stringResource(R.string.legal_pp_contact_intro))
    LContactLine(label = stringResource(R.string.legal_pp_contact_email_label), value = "lxb031018@163.com")
    LContactLine(label = stringResource(R.string.legal_pp_contact_github_label), value = "github.com/lxb031018/lishi/issues")
    LContactLine(label = stringResource(R.string.legal_pp_contact_project_label), value = "github.com/lxb031018/lishi")
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_contact_subject))
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_contact_response)) }
    })

    LRule()

    LH2(stringResource(R.string.legal_pp_section10))
    LH3(stringResource(R.string.legal_pp_section10_1))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_governing_law))
    })
    LH3(stringResource(R.string.legal_pp_section10_2))
    LPRich(buildAnnotatedString {
        append(stringResource(R.string.legal_pp_dispute_resolution))
    })

    LRule()

    LH2(stringResource(R.string.legal_pp_section11))
    LP(stringResource(R.string.legal_pp_effectiveness))

    LRule()

    LH2(stringResource(R.string.legal_pp_glossary))
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_glossary_label_personal_info)) }
        append(" ${stringResource(R.string.legal_pp_glossary_personal_info)}")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_glossary_label_sensitive_info)) }
        append(" ${stringResource(R.string.legal_pp_glossary_sensitive_info)}")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_glossary_label_collect)) }
        append(" ${stringResource(R.string.legal_pp_glossary_collect)}")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_glossary_label_share)) }
        append(" ${stringResource(R.string.legal_pp_glossary_share)}")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_glossary_label_sdk)) }
        append(" ${stringResource(R.string.legal_pp_glossary_sdk)}")
    })

    LRule()

    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append(stringResource(R.string.legal_pp_copyright)) }
    })
    LPRich(buildAnnotatedString {
        withStyle(italicSpan) { append(stringResource(R.string.legal_pp_reference)) }
    })
}

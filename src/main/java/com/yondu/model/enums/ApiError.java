package com.yondu.model.enums;

/** Contains error_codes from Rush API docs.
 *  See http://52.74.203.202/api/docs/loyalty/merchantapp/#api-Loyalty_Merchant_App
 *
 *  @author - m1d0rf33d
 */
public enum ApiError {

    x10("Invalid customer login details.");

    private String value;

    ApiError(String value) {
        this.value = value;
    }
}

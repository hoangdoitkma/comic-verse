package com.example.comicversev1.data.model;

import java.math.BigDecimal;

public class VipPackageDTO {
    public int id;
    public String name;
    public int durationMonth;
    public BigDecimal price;
    public String currency;
    public boolean isActive;
    
    public String getFormattedPrice() {
        if (price == null) return "0 " + currency;
        return String.format("%,.0f %s", price, currency != null ? currency : "VND");
    }
    
    public String getFormattedPeriod() {
        if (durationMonth == 1) return " / tháng";
        if (durationMonth == 12) return " / năm";
        if (durationMonth == 999) return " / trọn đời";
        return " / " + durationMonth + " tháng";
    }
}

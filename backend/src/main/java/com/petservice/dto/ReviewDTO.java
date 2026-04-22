package com.petservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewDTO {
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "最低评分为1")
    @Max(value = 5, message = "最高评分为5")
    private Integer rating;
    private String content;
    private String images;

    public Integer getRating() { return rating; } public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public String getImages() { return images; } public void setImages(String images) { this.images = images; }
}

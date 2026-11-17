package edu.itc.shop_rest.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public class ProductFilter {

    @QueryParam("page")
    @DefaultValue("0")
    @Min(0)
    int page = 0;

    @QueryParam("size")
    @DefaultValue("20")
    @Min(1)
    @Max(100)
    int size = 20;
}
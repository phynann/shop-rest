package edu.itc.shop_rest.api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.*;
import edu.itc.shop_rest.domain.Product;

@XmlRootElement(name = "product")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductDto {
    @XmlAttribute
    public long id;

    @NotBlank
    @Size(max = 80)
    public String name;

    @NotNull
    @Pattern(regexp = "[A-Z]{2}-\\d{2}")
    public String sku;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    public BigDecimal price;

    @PositiveOrZero
    public int stock;

    public ProductDto() { }

    public static ProductDto from(Product p) {
        ProductDto dto = new ProductDto();
        dto.id = p.id();
        dto.name = p.name();
        dto.sku = p.sku();
        dto.price = p.price();
        dto.stock = p.stock();
        return dto;
    }

    public Product toProduct(long id, int version) {
        return new Product(id, name, sku, price, stock, version);
    }
}
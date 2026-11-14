package edu.itc.shop_rest.domain;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public class InsufficientStockException extends ClientErrorException {
    public InsufficientStockException(long id, int stock, int delta) {
        super("Product " + id + " has stock " + stock + ", cannot apply " + delta,
                Response.Status.CONFLICT);
    }
}
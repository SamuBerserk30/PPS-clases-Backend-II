package co.edu.cesde.pps.web.controller;

public final class ApiRoutes {

    public static final String API_V1 = "/api/v1";
    public static final String AUTH = API_V1 + "/auth";
    public static final String CATEGORIES = API_V1 + "/categories";
    public static final String PRODUCTS = API_V1 + "/products";
    public static final String USER_PROFILE = API_V1 + "/users/me";
    public static final String USER_ADDRESSES = USER_PROFILE + "/addresses";
    public static final String CART = API_V1 + "/cart";
    public static final String ORDERS = API_V1 + "/orders";
    public static final String ADMIN_USERS = API_V1 + "/admin/users";
    public static final String ADMIN_PRODUCTS = API_V1 + "/admin/products";

    private ApiRoutes() {
    }
}


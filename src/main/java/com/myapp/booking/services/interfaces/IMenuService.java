package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.requests.CreateMenuRequest;
import com.myapp.booking.dtos.responses.MenuResponse;

import java.util.List;

public interface IMenuService {

    MenuResponse createMenu(Long postId, CreateMenuRequest request, Long vendorId);

    List<MenuResponse> getMenusByPost(Long postId);

    MenuResponse updateMenu(Long menuId, CreateMenuRequest request, Long vendorId);

    void deleteMenu(Long menuId, Long vendorId);
}
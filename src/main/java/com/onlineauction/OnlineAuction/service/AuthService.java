package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.AuthReqDTO;
import com.onlineauction.OnlineAuction.dto.AuthResDTO;
import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface AuthService {
    AuthResDTO authenticateUser(AuthReqDTO authenticationRequest, HttpServletResponse response) throws Exception;
    AuthResDTO refreshToken(HttpServletRequest request, HttpServletResponse response) throws UserException;
    Map<String, String> registerUser(UserDTO userDTO) throws UserException;
}

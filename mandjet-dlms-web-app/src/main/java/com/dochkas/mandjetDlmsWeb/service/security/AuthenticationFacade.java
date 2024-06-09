package com.dochkas.mandjetDlmsWeb.service.security;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    Authentication getAuthentication();
}

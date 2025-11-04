package com.xk.truck.upms.domain.service;

import com.xk.base.exception.BusinessException;
import com.xk.truck.upms.controller.api.dto.permission.PermissionResp;
import com.xk.truck.upms.controller.api.dto.role.RoleCreateReq;
import com.xk.truck.upms.controller.api.dto.role.RoleResp;
import com.xk.truck.upms.controller.api.dto.user.UserCreateReq;
import com.xk.truck.upms.controller.api.dto.user.UserResp;
import com.xk.truck.upms.controller.api.dto.user.UserUpdateReq;
import com.xk.truck.upms.domain.dao.repository.RoleRepository;
import com.xk.truck.upms.domain.dao.repository.UserRepository;
import com.xk.truck.upms.domain.model.po.Role;
import com.xk.truck.upms.domain.model.po.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepo;
    @Mock
    RoleRepository roleRepo;
    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    UserService userService;
    @InjectMocks
    RoleService roleService;

    @Test
    @DisplayName("create：新帳號建立成功，且會加密密碼與賦予角色")
    void create_ok() {var req = new UserCreateReq();
        req.setUsername("alice");
        req.setPassword("pw123");
        req.setRoleCodes(Set.of("ADMIN"));

        var role = new Role();
        role.setCode("ADMIN");

        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(roleRepo.findByCodeIn(Set.of("ADMIN"))).thenReturn(Set.of(role));
        when(encoder.encode("pw123")).thenReturn("ENC_pw123");

        // 模擬 save：回存時塞一個 uuid
        Answer<User> saveAnswer = inv -> {
            User u = inv.getArgument(0);
            if (u.getUuid() == null) u.setUuid(UUID.randomUUID());
            return u;
        };
        when(userRepo.save(any(User.class))).thenAnswer(saveAnswer);

        UserResp resp = userService.create(req);

        assertThat(resp.getUsername()).isEqualTo("alice");
        // 其他欄位可再驗證
    }

    @Test
    @DisplayName("create：帳號已存在應拋 BusinessException")
    void create_exists() {
        // given
        UserCreateReq req = new UserCreateReq();
        req.setUsername("alice");
        given(userRepo.existsByUsername("alice")).willReturn(true);

        // expect
        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("帳號已存在");
    }

    @Test
    @DisplayName("list：回傳所有使用者")
    void list_ok() {
        var u1 = new User("a", "x", true);
        var u2 = new User("b", "y", true);

        var pageable = PageRequest.of(0, 10);
        when(userRepo.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(u1, u2), pageable, 2));

        Page<UserResp> page = userService.list(pageable);

        // Page 不是 List，不能 get(0)；應該取 content
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getUsername()).isEqualTo("a");
    }

    @Test
    @DisplayName("enable：可切換啟用狀態")
    void enable_ok() {
        UUID id = UUID.randomUUID();
        User u = new User("a", "x", true);
        given(userRepo.findById(id)).willReturn(Optional.of(u));

        var resp = userService.enable(id, false);
        assertThat(resp.getEnabled()).isFalse();
        then(userRepo).should().save(u);
    }

    @Test
    @DisplayName("resetPassword：會呼叫 encoder 並保存")
    void resetPassword_ok() {
        UUID id = UUID.randomUUID();
        User u = new User("a", "ENC", true);
        given(userRepo.findById(id)).willReturn(Optional.of(u));
        given(encoder.encode("new")).willReturn("ENC_NEW");

        userService.resetPassword(id, "new");
        then(userRepo).should().save(u);
        assertThat(u.getPassword()).isEqualTo("ENC_NEW");
    }

    @Test
    @DisplayName("update：可更新 username 與角色")
    void update_ok() {
        UUID id = UUID.randomUUID();
        User u = new User("a", "x", true);
        given(userRepo.findById(id)).willReturn(Optional.of(u));

        Role r = new Role();
        r.setCode("ADMIN");
        given(roleRepo.findByCodeIn(Set.of("ADMIN"))).willReturn(Set.of(r));

        UserUpdateReq req = new UserUpdateReq();
        req.setUsername("alice");
        req.setRoleCodes(Set.of("ADMIN"));

        given(userRepo.save(any(User.class))).willReturn(u);

        var resp = userService.update(id, req);
        assertThat(resp.getUsername()).isEqualTo("alice");
        assertThat(u.getRoles()).extracting(Role::getCode).containsExactly("ADMIN");
    }

    @Test
    void role_create_ok() {
        var req = new RoleCreateReq();
        req.setCode("DISPATCH");
        req.setName("調度");

        when(roleRepo.existsByCode("DISPATCH")).thenReturn(false);

        Answer<Role> saveAnswer = inv -> {
            Role r = inv.getArgument(0);
            return r; // 模擬成功保存
        };
        when(roleRepo.save(any(Role.class))).thenAnswer(saveAnswer);

        var resp = roleService.create(req);
        assertThat(resp.getCode()).isEqualTo("DISPATCH");
    }

}

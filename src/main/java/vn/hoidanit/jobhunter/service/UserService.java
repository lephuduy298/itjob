package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.MyCv;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.UserProfile;
import vn.hoidanit.jobhunter.domain.request.AdminUpdateUserRequest;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.RoleRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.constant.StatusEnum;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyService companyService;
    private final RoleService roleService;
    private final UserProfileService userProfileService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       CompanyService companyService,
                       RoleService roleService,
                       UserProfileService userProfileService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyService = companyService;
        this.roleService = roleService;
        this.userProfileService = userProfileService;
    }

    public User handleCreateUser(User user) {
        // check company
        if (user.getCompany() != null) {
            Optional<Company> companyOptional = this.companyService.findById(user.getCompany().getId());
            user.setCompany(companyOptional.isPresent() ? companyOptional.get() : null);
        }

        // check role
        if (user.getRole() != null) {
            Role r = this.roleService.fetchById(user.getRole().getId());
            user.setRole(r != null ? r : null);
        }

        User savedUser = this.userRepository.save(user);

        return savedUser;
    }

    @Transactional
    public void handleDeleteUser(long id) {

        if(this.userProfileService.getUserProfileByUserId(id) !=null){
            this.userProfileService.handleDeleteUserProfile(id);
        }

        this.userRepository.deleteById(id);
    }

    public User fetchUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllUser(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        rs.setMeta(mt);

        // remove sensitive data
        List<ResUserDTO> listUser = pageUser.getContent()
                .stream().map(item -> this.convertToResUserDTO(item))
                .collect(Collectors.toList());

        rs.setResult(listUser);

        return rs;
    }

    public User handleUpdateUser(Long id, User reqUser) {
        log.info("Request user for update: {}", reqUser);
        User currentUser = this.fetchUserById(id);
        if (currentUser != null) {
            if (reqUser.getEmail() != null && !reqUser.getEmail().isBlank()) {
                currentUser.setEmail(reqUser.getEmail());
            }
            if (reqUser.getAddress() != null && !reqUser.getAddress().isBlank()) {
                currentUser.setAddress(reqUser.getAddress());
            }
            if (reqUser.getGender() != null) {
                currentUser.setGender(reqUser.getGender());
            }
            if (reqUser.getAge() != 0) {
                currentUser.setAge(reqUser.getAge());
            }
            if (reqUser.getName() != null && !reqUser.getName().isBlank()) {
                currentUser.setName(reqUser.getName());
            }
            currentUser.setAvatar(reqUser.getAvatar());
            if (reqUser.getDateOfBirth() != null) {
                currentUser.setDateOfBirth(reqUser.getDateOfBirth());
            }
            if (reqUser.getPhoneNumber() != null && !reqUser.getPhoneNumber().isBlank()) {
                currentUser.setPhoneNumber(reqUser.getPhoneNumber());
            }

            // check company
            if (reqUser.getCompany() != null) {
                Optional<Company> companyOptional = this.companyService.findById(reqUser.getCompany().getId());
                currentUser.setCompany(companyOptional.isPresent() ? companyOptional.get() : null);
            }

            // check role
            if (reqUser.getRole() != null) {
                Role r = this.roleService.fetchById(reqUser.getRole().getId());
                currentUser.setRole(r != null ? r : null);
            }

            // update
            currentUser = this.userRepository.save(currentUser);
            log.info("User after update: {}", currentUser);
        }
        return currentUser;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();
        ResCreateUserDTO.CompanyUser com = new ResCreateUserDTO.CompanyUser();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());

        if (user.getCompany() != null) {
            com.setId(user.getCompany().getId());
            com.setName(user.getCompany().getName());
            res.setCompany(com);
        }
        return res;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();
        ResUpdateUserDTO.CompanyUser com = new ResUpdateUserDTO.CompanyUser();
        if (user.getCompany() != null) {
            com.setId(user.getCompany().getId());
            com.setName(user.getCompany().getName());
            res.setCompany(com);
        }

        res.setId(user.getId());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        return res;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        ResUserDTO.CompanyUser com = new ResUserDTO.CompanyUser();
        ResUserDTO.RoleUser roleUser = new ResUserDTO.RoleUser();
        if (user.getCompany() != null) {
            com.setId(user.getCompany().getId());
            com.setName(user.getCompany().getName());
            res.setCompany(com);
        }

        if (user.getRole() != null) {
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            res.setRole(roleUser);
        }

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setGender(user.getGender());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setCreatedAt(user.getCreatedAt());
        res.setStatus(user.getStatus());
        res.setAddress(user.getAddress());
        return res;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }

    public void verifyUserByEmail(String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setVerified(true);
            currentUser.setStatus(StatusEnum.ACTIVE);
            this.userRepository.save(currentUser);
            this.userProfileService.createUserProfileForUser(currentUser);

        }
    }

    public void changeUserPassword(User user, String hashPassword) {
        // TODO Auto-generated method stub
        user.setPassword(hashPassword);
        this.userRepository.save(user);
    }

    public void handleAddFavoriteJobIds(Long id) {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUserDB = this.handleGetUserByUsername(email);
        if (currentUserDB != null) {
            List<Long> favoriteJobIds = currentUserDB.getFavoriteJobIds();
            if (favoriteJobIds == null) {
                favoriteJobIds = new java.util.ArrayList<>();
            }
            if (!favoriteJobIds.contains(id)) {
                favoriteJobIds.add(id);
                currentUserDB.setFavoriteJobIds(favoriteJobIds);
                log.info("Added job id {} to favorite list", id);

            } else {
                currentUserDB.getFavoriteJobIds().remove(id);
                log.info("Removed job id {} from favorite list", id);
            }
            this.userRepository.save(currentUserDB);

        }
    }
    public void applyDefaultRegisterState(User user) {
        user.setVerified(false);
        user.setStatus(StatusEnum.PENDING);

        // set role mặc định USER nếu chưa có
        if (user.getRole() == null) {
            Role roleUser = roleService.getRoleByName("USER");
            user.setRole(roleUser);
        }
    }

    public void applyDefaultRegisterStateWithGoogle(User user) {
        user.setVerified(true);
        user.setStatus(StatusEnum.ACTIVE);

        // set role mặc định USER nếu chưa có
        if (user.getRole() == null) {
            Role roleUser = roleService.getRoleByName("USER");
            user.setRole(roleUser);
        }
    }

    public User handleUpdateUserByAdmin(Long id, AdminUpdateUserRequest req) {

        User currentUser = fetchUserById(id);
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }

        if (req.getStatus() != null) {
            currentUser.setStatus(req.getStatus());
        }

        if (req.getRoleId() != null) {
            Role r = roleService.fetchById(req.getRoleId());
            if (r == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + req.getRoleId());
            }
            currentUser.setRole(r);
        }

        return userRepository.save(currentUser);
    }

    public void updateLastSeen(Long userId, java.time.Instant lastSeen) {
        User user = fetchUserById(userId);
        if (user != null) {
            user.setLastSeen(lastSeen);
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateRoleAfterPurchase(Long userId, String audience, String tier) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        String roleName;

        if ("USER".equalsIgnoreCase(audience)) {
            roleName = "STANDARD".equalsIgnoreCase(tier)
                    ? "USER_VIP"
                    : "USER";
        }
        else if ("HR".equalsIgnoreCase(audience)) {
            roleName = "STANDARD".equalsIgnoreCase(tier)
                    ? "HR_VIP"
                    : "HR";
        }
        else {
            return;
        }

        Role newRole = roleRepository.findByName(roleName);

        if (user.getRole() != null && user.getRole().getId() == newRole.getId()) {
            return;
        }

        user.setRole(newRole);
        userRepository.save(user);
    }
}
package vn.hoidanit.jobhunter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.UserProfile;
import vn.hoidanit.jobhunter.repository.UserProfileRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public void createUserProfileForUser(User user) {
        // Check if UserProfile already exists for this user
        UserProfile existingProfile = this.userProfileRepository.findByUserId(user.getId());
        if (existingProfile != null) {
            log.info("UserProfile already exists for user id: {}", user.getId());
            return;
        }

        // Create new UserProfile
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        this.userProfileRepository.save(userProfile);
        log.info("Created new UserProfile for user id: {}", user.getId());
    }

    public UserProfile updateUserProfile(Long id, UserProfile userProfile) {
        log.info("UserProfile after update: {}", userProfile);
        UserProfile existingProfile = this.userProfileRepository.findByUserId(id);
        if (existingProfile != null) {
            if (userProfile.getTitle() != null) {
                existingProfile.setTitle(userProfile.getTitle());
            }
            if (userProfile.getExperience() != null) {
                existingProfile.setExperience(userProfile.getExperience());
            }
            if (userProfile.getEducation() != null) {
                existingProfile.setEducation(userProfile.getEducation());
            }
            if (userProfile.getNationality() != null) {
                existingProfile.setNationality(userProfile.getNationality());
            }
            if (userProfile.getMaritalStatus() != null) {
                existingProfile.setMaritalStatus(userProfile.getMaritalStatus());
            }
            if (userProfile.getPersonalWebsite() != null) {
                existingProfile.setPersonalWebsite(userProfile.getPersonalWebsite());
            }
            if (userProfile.getBiography() != null) {
                existingProfile.setBiography(userProfile.getBiography());
            }
            if (userProfile.getFacebookLink() != null) {
                existingProfile.setFacebookLink(userProfile.getFacebookLink());
            }
            if (userProfile.getLinkedinLink() != null) {
                existingProfile.setLinkedinLink(userProfile.getLinkedinLink());
            }
            if (userProfile.getTwitterLink() != null) {
                existingProfile.setTwitterLink(userProfile.getTwitterLink());
            }
            if (userProfile.getGithubLink() != null) {
                existingProfile.setGithubLink(userProfile.getGithubLink());
            }

            if(userProfile.getSkills() != null) {
                existingProfile.setSkills(userProfile.getSkills());
            }

            return this.userProfileRepository.save(existingProfile);
        }
        return null;
    }

    public UserProfile getUserProfileById(Long id) {
        return this.userProfileRepository.findByUserId(id);
    }

    @Transactional
    public void handleDeleteUserProfile(long id) {
        userProfileRepository.deleteByUserId(id);
    }


    public UserProfile getUserProfileByUserId(Long user_id){
        return  this.userProfileRepository.findByUserId(user_id);
    }

}

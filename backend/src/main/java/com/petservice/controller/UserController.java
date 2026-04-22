package com.petservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petservice.common.Result;
import com.petservice.entity.Address;
import com.petservice.entity.Pet;
import com.petservice.entity.User;
import com.petservice.service.AddressService;
import com.petservice.service.PetService;
import com.petservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private PetService petService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public Result<User> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User user, Authentication auth) {
        User currentUser = (User) auth.getPrincipal();
        currentUser.setAvatar(user.getAvatar());
        currentUser.setRealName(user.getRealName());
        currentUser.setIdCard(user.getIdCard());
        currentUser.setShopName(user.getShopName());
        userService.updateById(currentUser);
        return Result.success();
    }

    @GetMapping("/pets")
    public Result<?> getPets(Authentication auth) {
        Long userId = ((User) auth.getPrincipal()).getId();
        return Result.success(petService.list(new LambdaQueryWrapper<Pet>().eq(Pet::getUserId, userId)));
    }

    @PostMapping("/pet")
    public Result<Void> addPet(@RequestBody Pet pet, Authentication auth) {
        pet.setUserId(((User) auth.getPrincipal()).getId());
        petService.save(pet);
        return Result.success();
    }

    @PutMapping("/pet/{id}")
    public Result<Void> updatePet(@PathVariable Long id, @RequestBody Pet pet, Authentication auth) {
        Pet existing = petService.getById(id);
        if (existing == null || !existing.getUserId().equals(((User) auth.getPrincipal()).getId()))
            return Result.error("无权操作");
        pet.setId(id);
        petService.updateById(pet);
        return Result.success();
    }

    @DeleteMapping("/pet/{id}")
    public Result<Void> deletePet(@PathVariable Long id, Authentication auth) {
        Pet existing = petService.getById(id);
        if (existing == null || !existing.getUserId().equals(((User) auth.getPrincipal()).getId()))
            return Result.error("无权操作");
        petService.removeById(id);
        return Result.success();
    }

    @GetMapping("/address")
    public Result<?> getAddresses(Authentication auth) {
        return Result.success(addressService.list(new LambdaQueryWrapper<Address>()
            .eq(Address::getUserId, ((User) auth.getPrincipal()).getId())
            .orderByDesc(Address::getIsDefault)));
    }
}

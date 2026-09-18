package br.com.sistemabancario.api.database.entity;
import br.com.sistemabancario.api.enums.Role;
import java.util.Collection;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "usuario")
@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class UserEntity implements UserDetails{ 


    @Id 
    @GeneratedValue (strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    @JsonIgnore 
    private String senhaHash;
     
    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.role == Role.ADMIN){
            return Arrays.asList (new SimpleGrantedAuthority("ROLE_ADMIN"), 
            new SimpleGrantedAuthority("ROLE_OPERADOR"));
          }
        return Arrays.asList ( new SimpleGrantedAuthority("ROLE_OPERADOR"));
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
       return senhaHash;
    }

    @Override
    public boolean isAccountNonExpired() {
       return true;
    }

    @Override
    public boolean isAccountNonLocked() {
       return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
       return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }



}

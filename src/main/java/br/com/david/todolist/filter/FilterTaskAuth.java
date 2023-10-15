package br.com.david.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.david.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    public IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                var serverletPath = request.getServletPath();

                if (serverletPath.startsWith("/tasks/")){
        
                    var authorization = request.getHeader("Authorization");

                    var authEncoded = authorization.substring("Basic".length()).trim();

                    byte[] authDecode = Base64.getDecoder().decode(authEncoded);
                
                    var authString = new String(authDecode);

                    String[] credentials = authString.split(":");
                    String username = credentials[0];
                    String password = credentials[1];   
                 
                    var user = userRepository.findByUsername(username);

                    if (user == null) {
                        response.sendError(401);
                    } else {
                        var pwdVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                        if (pwdVerify.verified) {
                            request.setAttribute("idUser", user.getId());
                            filterChain.doFilter(request, response);
                        } else {
                            response.sendError(401);
                        }
                    }
                    
                } else {
                     filterChain.doFilter(request, response);
                }
    }

}

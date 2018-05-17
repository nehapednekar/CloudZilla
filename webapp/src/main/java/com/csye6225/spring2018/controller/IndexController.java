package com.csye6225.spring2018.controller;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.csye6225.spring2018.DAO.UserDao;
import com.csye6225.spring2018.pojo.Image;
import com.csye6225.spring2018.pojo.User;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.*;

@Controller
@Profile("dev")
public class IndexController {

    private final static Logger logger = LoggerFactory.getLogger(IndexController.class);


    @Autowired
    UserDao userDao;

    private String bucketName="s3.csye6225-spring2018-guhar.me.tld";
    @RequestMapping("/")
    public String index(HttpServletRequest request) {
        logger.info("Loading home page.");
        HttpSession session = (HttpSession) request.getSession();

        if(session.getAttribute("loggedin")!= null)
            return "homepage";
        else
            return "index";
    }



    @RequestMapping(value = "/searchuser", method = RequestMethod.POST)
    public String userSearch(HttpServletRequest request) {

        HttpSession session = (HttpSession) request.getSession();


        List<User> userList = userDao.findByUsername(request.getParameter("username"));


        if (userList.size() != 0) {

            User foundUser = userList.get(0);
            session.setAttribute("puser", foundUser.getUsername());
            session.setAttribute("text", foundUser.getBio());
            System.out.println("Image path in search:::" + foundUser.getImageName());
            session.setAttribute("image", foundUser.getImageName());
            return "getprofile";

        }


        else return "error_login";

        }


        @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(HttpServletRequest request) {
        HttpSession session = (HttpSession) request.getSession();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


        List<User> userList = userDao.findByUsername(request.getParameter("username"));

        if (userList.size() != 0) {
            User foundUser = userList.get(0);


            if (encoder.matches(request.getParameter("password"), foundUser.getPassword())) {

                session.setAttribute("puser",foundUser.getUsername());
                session.setAttribute("loggedin","true");
                session.setAttribute("currentUser", foundUser);
                session.setAttribute("text", foundUser.getBio());
                session.setAttribute("image", foundUser.getImageName());
                return "homepage";
            } else
                return "error_login";
        }

        return "error_login";
    }


    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(HttpServletRequest request) {

        return "register";

    }



    @RequestMapping(value = "/forgotpassword", method = RequestMethod.GET)
    public String forgotpasswordget(HttpServletRequest request) {

        return "forgotpassword";

    }



    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(HttpServletRequest request) {

        return "search";

    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public String userregister(HttpServletRequest request) {

        HttpSession session = (HttpSession) request.getSession();


        List<User> userList = userDao.findByUsername(request.getParameter("username"));

        if (userList.size() == 0) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String password = request.getParameter("password");
            String hashedpassword = passwordEncoder.encode(password);

            User user = new User();
            user.setUsername(request.getParameter("username"));
            user.setPassword(hashedpassword);
            userDao.save(user);

            return "registrationSuccessful";

        } else
            return "user_exists";
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home() {
        logger.info("Home Page");
        return "homepage";

    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request) {

        request.getSession().invalidate();
        logger.info("Logout Page");
        return "index";

    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(HttpServletRequest request) {


        logger.info("Profile Page");
        return "profile";

    }

    @RequestMapping(value = "/aboutMe", method = RequestMethod.POST)
    public String addBio(HttpServletRequest request) {

        HttpSession session = (HttpSession) request.getSession();

        String text = request.getParameter("bio");
        logger.info("About me section is " + text);
        User user = (User)request.getSession().getAttribute("currentUser");
        user.setBio(text);
        userDao.save(user);
        request.getSession().setAttribute("text", text);
        logger.info("Profile Page");
        return "profile";

    }

    @RequestMapping(value = "/saveImage", method = RequestMethod.POST)
    public String imageUpload(HttpServletRequest request, @ModelAttribute Image image, Model model, @RequestParam("images") MultipartFile multipartFile)  throws IOException {


            HttpSession session = (HttpSession) request.getSession();

            User user = (User)request.getSession().getAttribute("currentUser");


                    String fileName = multipartFile.getOriginalFilename();
                    request.getSession().setAttribute("image", fileName);


                    String filePath = request.getServletContext().getRealPath("/image");
                    File targetFilePath = new File(filePath);
                    File targetFile = new File(targetFilePath, fileName);



                    String srcFilePath = filePath.replace("ROOT", "web-app");
                    File dirUpdated = new File(srcFilePath);
                    File srcImageFile = new File(new File(srcFilePath), fileName);

                    if (!dirUpdated.exists()) {
                        dirUpdated.mkdirs();
                    }



                    request.getSession().setAttribute("imagePath", srcFilePath);
                    request.getSession().setAttribute("fullImagePath", srcImageFile);

                    //To set pic upon login

                    String picUrl="";
                    if (!targetFilePath.exists()) {
                        targetFilePath.mkdirs();
                    }
                    try {
                        picUrl = "image/" + fileName;
//                        picUrl = srcImageFile + fileName;
                        // picUrl="https://s3.amazonaws.com/"+bucketName+"/"+fileName;
                        multipartFile.transferTo(srcImageFile);
                        FileUtils.copyFile(srcImageFile, targetFile);
                        request.getSession().setAttribute("imageDB", picUrl);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("imagename:::" + fileName);

 //                   user.setImage(srcImageFile);
                    user.setImageName(picUrl);


                    session.setAttribute("image", picUrl);

                    System.out.println("Pic URL = "+picUrl);
                    userDao.save(user);

                    System.out.println("The user image name is" + user.getImageName());




        logger.info("Profile Page");

//            model.addAttribute("imageDB", "/home/ayon/Downloads/apache-tomcat-8.5.27/webapps/web-app/image/");
            return "profile";
        }


    @RequestMapping(value = "/deleteImage", method = RequestMethod.GET)
    public String deleteImage(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        User user = (User)session.getAttribute("currentUser");
        user.setImageName(null);
        userDao.save(user);
        session.setAttribute("image", null);
        return "profile";
    }



    /////////////////////............USING JSON..............///////////////////////////////////



// Base mapping

    @RequestMapping(value = "/post/", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String checkSession()
    {
        JsonObject jsonObject = new JsonObject();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth !=null && auth instanceof AnonymousAuthenticationToken)

            jsonObject.addProperty("message","You are not logged in! Enter username and password");
        else
            jsonObject.addProperty("message","You are logged in. Current Time is "+new Date().toString());


        return jsonObject.toString();
    }







//User Log in

    @RequestMapping(value = "/post/login", method = RequestMethod.POST, produces = {"application/json"},
            consumes = "application/json", headers = {"content-type=application/json; charset=utf-8"})
    @ResponseBody
    public String postLogin(@RequestBody User user) {



        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        JsonObject jsonObject = new JsonObject();


        List<User> userList = userDao.findByUsername(user.getUsername());

        if (userList.size() != 0) {
            User foundUser = userList.get(0);

            System.out.println("User was found!! His username is " + foundUser.getUsername()
                    + " and his password is "
                    + foundUser.getPassword());

            if (encoder.matches(user.getPassword(), foundUser.getPassword())) {
                System.out.println("Size of the list is =" + userList.size());

                jsonObject.addProperty("message", "success");
                jsonObject.addProperty("time", new Date().toString());
            } else jsonObject.addProperty("message", "Incorrect Password");


        } else jsonObject.addProperty("message", "User not found!");

        return jsonObject.toString();


    }


    @RequestMapping(value = "/user/forgotpassword", method = RequestMethod.GET)
    public String forgotPasswordGet(HttpServletRequest request) {

        return "forgotpassword";

    }


    @RequestMapping(value = "/user/forgotpassword", method = RequestMethod.POST)
    public String forgotPassword(HttpServletRequest request) {

        return "emailsuccess";

        }

// User Registration

    @RequestMapping(value = "/post/register", method = RequestMethod.POST, produces = {"application/json"},
            consumes = "application/json", headers = {"content-type=application/json; charset=utf-8"})
    @ResponseBody
    public String postRegister(@RequestBody User user) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        JsonObject jsonObject = new JsonObject();


        List<User> userList = userDao.findByUsername(user.getUsername());

        if (userList.size() == 0) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String password = user.getPassword();
            String hashedpassword = passwordEncoder.encode(password);

            User u = new User();
            u.setUsername(user.getUsername());
            u.setPassword(hashedpassword);
            userDao.save(u);
            jsonObject.addProperty("message", "Registration Successful");


        } else jsonObject.addProperty("message", "User already exists!");

        return jsonObject.toString();


    }

}
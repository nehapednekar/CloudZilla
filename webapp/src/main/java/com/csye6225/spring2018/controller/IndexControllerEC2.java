package com.csye6225.spring2018.controller;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
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
@Profile("aws")
public class IndexControllerEC2 {

    private final static Logger logger = LoggerFactory.getLogger(IndexController.class);


    @Autowired
    UserDao userDao;


    private String bucketName="web-app-csye6225-spring2018-guhar.me"; //Enter bucket name which will contain the webapp code
    private String defaultImage="https://s3.amazonaws.com/"+bucketName+"/person.png"; //Enter a default image URL

    @RequestMapping("/")
    public String index(HttpServletRequest request) {
        logger.info("Loading home page.");
        HttpSession session = (HttpSession) request.getSession();
        session.setAttribute("image", defaultImage);
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
                if(foundUser.getImageName()!=null)
                    session.setAttribute("image", foundUser.getImageName());
                else
                    session.setAttribute("image", defaultImage);
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



    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(HttpServletRequest request) {

        return "search";

    }


    @RequestMapping(value = "/forgotpassword", method = RequestMethod.GET)
    public String forgotPasswordGet(HttpServletRequest request) {

        return "forgotpassword";

    }


    @RequestMapping(value = "/user/forgotpassword", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String passwordReset(@RequestParam(value = "username") String username) {
        logger.info("In the forgot password");
        JsonObject object = new JsonObject();
        List<User> user = userDao.findByUsername(username);
        object.addProperty("message: ", "A mail with a reset link has been sent to: " + username);

        System.out.println("The List is : " + user.size());
        System.out.println("The user is " + user);


            AmazonSNS sns = AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
            CreateTopicResult topicResult = sns.createTopic("ForgotPassword");

            String arn = topicResult.getTopicArn();//obj.get("TopicArn").getAsString();
            System.out.println("The ARN is :" + arn);
            logger.info("The ARN is :" + arn);
            PublishRequest publishRequest = new PublishRequest(arn, "" + username + "," + username);
            PublishResult publishResult = sns.publish(publishRequest);

            return object.toString();
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

        HttpSession session = (HttpSession) request.getSession();
        User user = (User)request.getSession().getAttribute("currentUser");
        session.setAttribute("text",user.getBio());
        logger.info("User Bio is "+user.getBio());


        if(user.getImageName()!=null)
            session.setAttribute("image",user.getImageName());

        logger.info("User image name is "+user.getImageName());

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

        Random rand = new Random();

        HttpSession session = (HttpSession) request.getSession();
        User user = (User) request.getSession().getAttribute("currentUser");

        String fileName = multipartFile.getOriginalFilename();

        if (!fileName.equals("")) {
            logger.info("File name is not null and is "+fileName);
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

            if (!targetFilePath.exists()) {
                targetFilePath.mkdirs();
            }
            //To set pic upon login

            String picUrl = "";
            picUrl = "https://s3.amazonaws.com/" + bucketName + "/" + fileName;
            user.setImageName(picUrl);
            multipartFile.transferTo(srcImageFile);
            FileUtils.copyFile(srcImageFile, targetFile);
            request.getSession().setAttribute("imageDB", picUrl);


            session.setAttribute("image", picUrl);
            userDao.save(user);

            //S3 Upload

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new InstanceProfileCredentialsProvider(false))
                    .build();
            try {
                System.out.println("Uploading a new object to S3 from a file\n");
                File file = new File(targetFile.toString());
                s3Client.putObject(new PutObjectRequest(
                        bucketName, fileName, file));
                logger.info("FileName for upload is "+fileName);

            } catch (AmazonServiceException ase) {

                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }
        } else {
            logger.info("No image uploaded");
            logger.info("The value received is " + session.getAttribute("image"));
            request.getSession().setAttribute("image", defaultImage);

        }


        logger.info("Profile Page");


        return "profile";
    }

    @RequestMapping(value = "/deleteImage", method = RequestMethod.GET)
    public String deleteImage(HttpServletRequest request){
        HttpSession session = request.getSession(false);

        try {

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new InstanceProfileCredentialsProvider(false))
                    .build();
            String filePath= (String) session.getAttribute("image");
            String[] filepatharr  =filePath.split("/");
            String fileName=filepatharr[filepatharr.length-1];

            System.out.println("In the delete block");
            System.out.println("File name is "+fileName+" and the bucket name is "+bucketName);

            s3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
            System.out.println("After calling the delete function");

        }
        catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        User user = (User)session.getAttribute("currentUser");
        user.setImageName(null);
        userDao.save(user);
        session.setAttribute("image", defaultImage);
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

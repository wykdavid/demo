# Qucik Orange Backend
production link:
http://3.137.170.120:8080/resources/listitem.html(List product page)
http://3.137.170.120:8080/resources/createitem.html(Create product page)
http://3.137.170.120:8080/resources/login.html(Login page)

This project is the source code of Qucik Orange Backend. To run this project on your device, you need to install redis and mysql first and run redis server and mysql server. The configuration file is called application.yml.
You can modify this file according to your personal condition. To access the service on local, you can go to the src/main/resources directory, there are several html files. Open any one of the files, then you will be able to access the corresponding service.

The register function is completed, you could use your SU email to register. Our system will generaze a otp code which you will need during your registera. 
However, we did not implement the email sending service, so the otp code will not be sent to your email. Instead, we just print the otp code on the console. 
In that case, if you want to register your own account on your local device, you can get you otp code by checking the console. If you want to view our online service, 
you could use account ywu268@syr.edu with password 123. If you do want to create your own account on our online service, you can use the test.pem file to connect to two cloud
servers.
The instruction is below:
1. Open an SSH client.
2. Locate your private key file. The key used to launch this instance is test.pem
3. Run command chmod 400 test.pem, if necessary, to ensure your key is not publicly viewable.
4. Connect to instance using its Public DNS:

 ec2-3-136-97-76.us-east-2.compute.amazonaws.com
 or
 ec2-user@ec2-3-14-246-130.us-east-2.compute.amazonaws.com
 
Example:

ssh -i "test.pem" ec2-user@ec2-3-136-97-76.us-east-2.compute.amazonaws.com 

ssh -i "test.pem" ec2-user@ec2-3-14-246-130.us-east-2.compute.amazonaws.com
 
 Use commond: sudo find // -name "nohup.out" to find the log of our java application. 
 
 Use tail commond to help you find your otp code in the log file nohup.out.
 
 


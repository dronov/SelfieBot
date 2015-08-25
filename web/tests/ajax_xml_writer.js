    function Endorser() {  
      // collect data from web form  
    var userid, password, name, country, zip, email, xmlString, data;   
    userid = "userid" 
    password = document.getElementById("password").value;  
    name = document.getElementById("name").value;  
    country = document.getElementById("country").value;  
    zip = document.getElementById("zip").value;  
    email = document.getElementById("email").value;  
      
    //creating XMLhttpRequest object  
     var xhr;  
         if (window.XMLHttpRequest) { // Mozilla, Safari, ...  
        xhr = new XMLHttpRequest();  
    } else if (window.ActiveXObject) { // IE 8 and older  
        xhr = new ActiveXObject("Microsoft.XMLHTTP");  
    }  
      
    //creating the xml string  
        xmlString = "<userinfo>" +  
        "  <userid>" + escape(userid) + "</userid>" +  
        "  <password>" + escape(password) + "</password>" +  
        "  <name>" + escape(name) + "</name>" +  
        "  <country>" + escape(country) + "</country>" +  
        "  <zip>" + escape(zip) + "</zip>" +  
        "  <email>" + escape(email) + "</email>" +  
        "</userinfo>";  
      
    //alert(data);  
      // Build the URL to connect to  
      var url = "save-userinfo.php";  
      
      // Open a connection to the server  
      xhr.open("POST", url, true);  
      
      // declaring that the data being sent is in XML format  
      xhr.setRequestHeader("Content-Type", "text/xml");  
      
      // Send the request  
      xhr.send(xmlString);  
    }  
package main.java.webConfig

val WebServer.html: String
    get() = """
<!DOCTYPE html>
<html lang="en">

<head>
  <title>JavaScript - read JSON from URL</title>
  <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
  <script>
    function setMode(mode){
      ${'$'}.getJSON('/setconfig?mode=' + mode, function(data) {
        document.getElementById("stateInfo").innerHTML = data.mode
      });
    }
    </script>
    <style>
      html{
          width:100%;
          height:100%;
      }
      body{
          width:100%;
          height:100%;
          transform: scale(3);
          transform-origin: 0 0;
          background-color:#DDD;
      }
      .button{
          background-color:#AAA;
      }
      </style>
</head>

<link href="style.css" rel="stylesheet">

<body>
  <h1>PowerManger</h1>
  <h2><div id="stateInfo"></div></h2><br>
  <script>
    ${'$'}.getJSON('/config', function(data) {
      document.getElementById("stateInfo").innerHTML = data.mode
    });
  </script>

  <p><input type="button" value="OFF" id="offbutton" onclick="setMode('OFF')" class="button"></p>
  <p><input type="button" value="PV"  id="pvbutton" onclick="setMode('PV')" class="button"></p>
  <p><input type="button" value="PV_WITH_MIN" id="pvwithminbutton" onclick="setMode('PV_WITH_MIN')" class="button"></p>
  <p><input type="button" value="MAX" id="maxbutton" onclick="setMode('MAX')" class="button"> </p>
</body>

</html>
"""

val WebServer.css: String
    get() = """
        @charset "UTF-8";
/* CSS Document */

* {
  margin: 0;
  padding: 0;
}

@font-face {
  font-family: "San Francisco";
  font-weight: 400;
  src: url("https://applesocial.s3.amazonaws.com/assets/styles/fonts/sanfrancisco/sanfranciscodisplay-regular-webfont.woff");
}

/* Hier bitte die Schrift einfügen, die du gerne hättest */

body {
  font-family: "Sans Source";
  background: #635F62;
}

h1 {
  font-size: 14px;
  color: #FFC125;
  margin-left: 10px;
  margin-top: 8px;
  margin-bottom: -8px;
}

h2 {
  font-size: 12px;
  color: #E5AD21;
  margin-left: 10px;
  margin-top: 8px;
  margin-bottom: -8px;
}

.button {
  font-size: 10px;
  width: 90px;
  margin-left: 10px;
  padding-bottom: 2px;
  margin-bottom: 2px;
  border: none;
  display: inline-block;
}

.button:focus {
  color: #02FDFD;
}

/* Ansicht Tablet */

@media screen and (min-width: 768px) {
  h1 {
    font-size: 24px;
    margin-left: 10px;
    margin-top: 8px;
    margin-bottom: -5px;
  }
  .button {
    font-size: 14px;
    width: 130px;
    padding-bottom: 2px;
    margin-bottom: 2px;
  }
}

/* Ansicht Computer */

@media screen and (min-width:992px) {
  h1 {
    font-size: 24px;
    margin-left: 10px;
    margin-top: 8px;
    margin-bottom: -6px;
  }
  .button {
    font-size: 10px;
    width: 100px;
    padding-bottom: 2px;
    margin-bottom: 2px;
  }
  .button {
    float: left;
    text-align: center;
    width: 90px;
    margin-top: 12px;
    padding: 4px;
  }
}
"""
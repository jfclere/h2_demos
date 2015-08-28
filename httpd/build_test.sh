rm -rf images
mkdir images
cat > http2.html << EOF
<!DOCTYPE html>
<html>
<head>
    <title>HTTP/2 DEMO</title>
</head>
<body>
EOF

i=0
while [ $i -lt 25 ]
do
  echo "<div id=\"row$i\" >" >> http2.html
  j=0
  while [ $j -lt 45 ]
  do 
    cp -p tomcat.png images/$i$j.png
    echo "<img height=\"20\" width=\"20\" src=\"images/$i$j.png\" />" >> http2.html
    j=`expr $j + 1`
  done
  echo "</div>" >> http2.html
  i=`expr $i + 1`
done

cat >> http2.html << EOF
</body>
</html>
EOF

#include<SoftwareSerial.h>
SoftwareSerial wifi(3,2);
char x;
int i,i1,i2;
char response[201];
void setup() {
  delay(5000);
  pinMode(10,OUTPUT); //Bulb
  pinMode(11,OUTPUT); //Fan
  pinMode(12,OUTPUT); //AC
  pinMode(13,OUTPUT); //for Testing
  Serial.begin(9600);
  Serial.println("Setting up everything...");
  wifi.begin(9600);
  while(!wifi);   //good practice
  Serial.println("WiFi module initalized...");
  Serial.println("Resetting wifi");
  wifi.print("AT\r\n");
  delay(2000);
  while(wifi.available()) Serial.print((char)wifi.read());
  Serial.println("Testing complete...");
  Serial.println("Putting wifi module to station mode...");
  wifi.print("AT+CWMODE=1\r\n");
  delay(2000);
  while(wifi.available()) Serial.print((char)wifi.read());
  Serial.println("Wifi module is in station mode...");
  delay(1000);
  Serial.println("Disconnecting from AP");
  wifi.print("AT+CWQAP\r\n");
  delay(2000);
  while(wifi.available()) Serial.print((char)wifi.read());
  Serial.println("Now wifi is not connected to any AP...");
  Serial.println("Connecting to TMIOTLAB");
  wifi.print("AT+CWJAP_CUR=\"Aishu\",\"aishwary99\"\r\n");
  x=1;
  while(x<=5)
  {
    delay(2000);
    while(wifi.available()) Serial.print((char)wifi.read());
    x++;  
  }
  Serial.println("Connected to TMIOTLAB");
}

void loop() {
  Serial.println("Connecting to IOTServer...");
  wifi.print("AT+CIPSTART=\"TCP\",\"192.168.43.22\",5050\r\n");
  delay(5000);
  while(wifi.available()) Serial.print((char)wifi.read());
  Serial.println("Connected to IOTServer...");
  Serial.println("Asking for commands from IOTServer");
  wifi.print("AT+CIPSEND=8\r\n");
  delay(4000);
  while(wifi.available()) Serial.print((char)wifi.read());
  wifi.print("BC,ECHO#");
  delay(5000);
  if(!wifi.find((char *)"+IPD,"))
  {
    while(wifi.available()) wifi.read();
    return; 
  }
  while(wifi.available() && wifi.read()!=':');
  i=0;
  Serial.print("Reading response.....");
  while(wifi.available() && i<200)
  {
    x=(char) wifi.read();
    if(x=='#') break;
    response[i]=x;
    i++;
  }
  response[i]='\0';
  //if(x=='#') return;
  Serial.println("Commands received from iot server");
  Serial.println("Following is the command to be executed");
  Serial.print("Length of command: ");
  Serial.println(i);
  if(i==0) return;
  Serial.print("Response from IOTServer - ");
  Serial.println(response);
  i2=0;
  while(true)
  {
    i1=i2;
    while(response[i2]!=',') i2++;
    response[i2]='\0';
    i2++;
    x=response[i2];
    if(strcmp(response+i1,"FAN")==0) 
    {
      if(x=='1') digitalWrite(13,HIGH);
      else if(x=='0') digitalWrite(13,LOW);
    }
    else if(strcmp(response+i1,"COOLER")==0) 
    {
      if(x=='1') digitalWrite(10,HIGH);
      else if(x=='0') digitalWrite(10,LOW);
    }
    else if(strcmp(response+i1,"FRIDGE")==0) 
    {
      if(x=='1') digitalWrite(11,HIGH);
      else if(x=='0') digitalWrite(11,LOW);
    }
    else if(strcmp(response+i1,"AC")==0) 
    {
      if(x=='1') digitalWrite(12,HIGH);
      else if(x=='0') digitalWrite(12,LOW);
    }
    i2++;
    if(response[i2]=='\0') break;
    i2++;
  }
}
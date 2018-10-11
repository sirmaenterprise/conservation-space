# Base Docker image for [vsftpd](https://security.appspot.com/vsftpd.html)

Lightweight and secure FTP server

## Example use:

`docker run -d -p 21:21 docker-reg.sirmaplatform.com/vsftpd:3.0.3`

To specify different ftp user/password use environment variables:

-e FTP_USER="ftpuser"
-e FTP_USER_PASSWORD="ftpuserpasswd"

To configure port range for pasv mode use:
-e PASV_MIN_PORT="4559"
-e PASV_MAX_PORT="4564"

Note that you must also map/expose that port range when running
-p 4559-4564:4559-4564
--expose 4559-4564

To be able to connect by IP in pasv mode use:
-e PASV_PROMISCUOUS="YES"

Use -e PASV_ADDRESS="public ip or hostname" to specify vsftpd host in passive mode if necessary

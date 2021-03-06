[한국어 (korean)](https://github.com/isac322/OneDrive-API-java/blob/master/README.kor.md) | **English**


# [OneDrive](https://onedrive.live.com/) API for Java

purse fast, easy to use, intuitive API.


### Supported Operation

- auto login authorization check and refresh
- fetching metadata of folder, file (by id and path)
- folder or file's metadata (size, name, path, children list and etc.)
- downloading file (sync and async)
- delete, copy, move, change metadata(name, description) of folder or file
- creating folder
- [Facets](https://dev.onedrive.com/facets/facets.htm) that OneDrive support like image, video..
- inquiring shared folder
- basic [RemoteItem](https://dev.onedrive.com/misc/working-with-links.htm) handling
- inquiring [Drives](https://dev.onedrive.com/resources/drive.htm)
- creating file and upload it (async)



### TODO

- searching file or folder (by name or content)
- sharing folder or file
- documentation
- support custom redirect url when login
- REST-api response error handling
- JRE6 version
- HTTPS GZIP support for synchronized operation


### Environment

- JRE7


### Dependency

*These are already included in gradle configuration file 'build.gradle'.*

- [Jackson](https://github.com/FasterXML/jackson)
- [Lombok](https://projectlombok.org/)
- [netty](http://netty.io/)


### Build

#### Windows

```cmd
gradlew.bat build
```


#### Linux, MacOS

```bash
./gradlew build
```

#### if [gradle](https://gradle.org/) is installed in your computer

```bash
gradle build
```


-----------------

## Simple example


### 1. Construct `Client` object

- All OneDrive jobs are performed via `Client` object.
- A program can contain multiple different `Client` object.
- Basically `Client` object check expiration and refresh authorization automatically. but it can be done manually.
- All parameters that pass to `Client`'s constructor can obtain if you fallow [OneDrive app instruction of authentication](https://dev.onedrive.com/app-registration.htm). 

```java
import com.bhyoo.Client;

String clientId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String[] scope = {"onedrive.readwrite", "offline_access", "onedrive.appfolder"};
String redirectURL = "http://localhost:8080/";
String clientSecret = "xxxxxxxxxxxxxxxxxxxxxxx";

// auto login
Client client = new Client(clientId, scope, redirectURL, clientSecret);
// self login
Client client = new Client(clientId, scope, redirectURL, clientSecret, false);
client.login();
```


### 2. Folder, file fetch

- It can conduct via either ID or path.
- `FolderItem` and `FileItem` are represent folder and file respectively.
- `FolderItem` and `FileItem` are child class of `BaseItem`.

```java
import com.bhyoo.container.items.FolderItem;
import com.bhyoo.container.items.BaseItem;
import com.bhyoo.container.items.pointer.PathPointer;

// assume that Client object is already constructed


// get root directory
FolderItem root = client.getRootDir();


// get folder by ID
FolderItem folder = client.getFolder("XXXXXXXXXXXXXXXX!XXXX");

// get folder by path
FolderItem folder1 = client.getFolder(new PathPointer("/{item-path}"));

// get file by ID
FileItem file = client.getFile("XXXXXXXXXXXXXXXX!XXXX");

// get file by path
FileItem file1 = client.getFile(new PathPointer("/{item-path}/{file-name}"));

// or if you don't know whether ID is file or folder
BaseItem item = client.getItem("XXXXXXXXXXXXXXXX!XXXX");

// or if you don't know whether path is file or folder
BaseItem item1 = client.getItem(new PathPointer("/{item-path}"));
```


### 3. get children of a folder

- `FolderItem` are `Iterable`. (it will returns child as `BaseItem`)
- Basically if `FolderItem` object is fetched by `Client`'s method `getFolder` or `getRootDir`, the object automatically fetchs children too. **(If children list is very long, it could take long time)**
- If you call `FolderItem`'s method `getAllChildren` or `getFolderChildren` or `getFileChildren`, you can get `List` of all children, only folder children, only file children respectively.
- if you call above methods, it will load children data and cache it. so **First call of those methods can take long time.**

```java
import com.bhyoo.container.items.*;

// assume that Client object is already constructed
FolderItem root = client.getRootDir();

List<BaseItem> children = root.getAllChildren();
List<FolderItem> folderChildren = root.getFolderChildren();
List<FileItem> fileChildren = root.getFileChildren();
```


### 4. Create folder

- It can create via either parent's `FolderItem` object or `Client` object.
- It will return created folder's `FolderItem` object.

```java
import com.bhyoo.container.items.FolderItem;
import com.bhyoo.container.items.pointer.PathPointer;

// assume that Client object is already constructed

FolderItem root = client.getRootDir();

// create folder by parent folder object
FolderItem newFolder = root.createFolder("test");


// create folder by client with parent folder id
FolderItem newFolder1 = client.createFolder("XXXXXXXXXXXXXXXX!XXXX", "test1");

// create folder by client with parent folder path
FolderItem newFolder2 = client.createFolder(new PathPointer("/"), "test2");
```


### 5. Copy folder or file

- It can copy via either source item's object or `Client` object.

```java
import com.bhyoo.container.items.*;
import com.bhyoo.container.items.pointer.*;

// assume that Client object is already constructed

BaseItem item = client.getItem("XXXXXXXXXXXXXXXX!XXXX");
FolderItem destination = client.getFolder("XXXXXXXXXXXXXXXX!XXXX");

// direct copy
item.copyTo(destination);
// direct copy with new name
item.copyTo(destination, "newName");

// copy by reference object
item.copyTo(destination.newReference());
// copy by reference object with new name
item.copyTo(destination.newReference(), "newName");

// copy by path string
item.copyTo(destination.getPathPointer());
// copy by path string with new name
item.copyTo(destination.getPathPointer(), "newName");

// copy by id string
item.copyTo(destination.getId());
// copy by id string with new name
item.copyTo(destination.getId(), "newName");


// using `Client`, copy by path
client.copyItem(new PathPointer("/{item-path}"), new IdPointer("XXXXXXXXXXXXXXXX!XXXX"));
```


### 6. Download file synchronously

```java
import java.nio.file.Paths;
import com.bhyoo.container.items.FileItem;

// assume that Client object is already constructed
FileItem file = client.getFile("XXXXXXXXXXXXXXXX!XXXX");

String path = "/home/isac322/download";

// download by system path string with original file name
file.download(path);

// download by system path string with new name
file.download(path, "newName");


// download by path object with original file name
file.download(Paths.get(path));

// download by path object with new name
file.download(Paths.get(path), "newName");


client.download(new PathPointer("/{item-path}"), Paths.get(path));
```

### 7. Download file asynchronously

- all async job use Future & Promise mechanism.
- more detail of `DownloadFuture` will explain later at wiki...

```java
import java.nio.file.Paths;
import com.bhyoo.container.items.FileItem;
import com.bhyoo.network.async.DownloadFuture;

// assume that Client object is already constructed

FileItem file = client.getFile("XXXXXXXXXXXXXXXX!XXXX");
String path = "/home/isac322/download";

// download by path object with original file name
file.downloadAsync(Paths.get(path));

// download by path object with new name
file.downloadAsync(Paths.get(path), "newName");


DownloadFuture future = client.downloadAsync("{file-id}", Paths.get(path), "newName");

// wait until download is done
future.sync();
```


### 8. Move folder or file

- It can move via either source item's object or `Client` object.

```java
import com.bhyoo.container.items.BaseItem;
import com.bhyoo.container.items.pointer.*;

// assume that Client object is already constructed

BaseItem item = client.getItem("XXXXXXXXXXXXXXXX!XXXX");
FolderItem destination = client.getFolder("XXXXXXXXXXXXXXXX!XXXX");

// direct move
item.moveTo(destination);

// move by reference object
item.moveTo(destination.newReference());

// move by path string
item.moveTo(destination.getPathPointer());

// move by id string
item.moveTo(destination.getId());


// using `Client` object, move by folder path
client.moveItem(new PathPointer("/{item-path}"), new IdPointer("XXXXXXXXXXXXXXXX!XXXX"));
```

### 9. Update folder or file's metadata & Refresh

- `refresh` will update all variable with fetched latest metadata. 
- That is, if `refresh` is invoked, all variable can be changed, even if the current program did not modify the variables.

```java
import com.bhyoo.container.items.BaseItem;

// assume that Client object is already constructed
BaseItem item = client.getItem("XXXXXXXXXXXXXXXX!XXXX");

// change item's name and flush to server.
item.setName("new name");


// change item's description and flush to server.
item.setDescription("blah blah");


// refresh item's all variable to latest value
item.refresh();
```

### 10. Upload file (asynchronously)

- like [asynchronous downloading](#7-download-file-asynchronously), it uses Future & Promise mechanism.
- more detail of `UploadFuture` will explain later at wiki...

```java
import java.nio.file.Path;
import com.bhyoo.network.async.UploadFuture;

// assume that Client object is already constructed

UploadFuture future;

// start to upload file
future = client.uploadFile("{remote-folder-id}", Paths.get("local-file-path"));
// wait until uploading is done
future.syncUninterruptibly();

```
# Socket Programming & AIDL을 이용한 UIDistribution

## 1. Introduction

본 프로젝트는 Multi Device 환경에서 Host 역할을 하는 디바이스와 Guest 역할을 하는 디바이스가 local network를 통해 
서로 UI를 주고 받으면서 interaction하는 구조로 설계되어 있다. 
사용자가 Host Device에서 Trigger(Ditribute할 UI 선택하는 action)이 발생하면 해당 UI가 Guest Device로 옮겨지면서 Guest Device의 화면에 출력이 되고, 양방향으로 
서로 interaction이 가능하여 UI의 update가 발생할 때 Host와 Guest Device는 서로 같은 data를 공유하게 된다.

## 2. System Design

본 프로젝트는 3개의 App으로 구성 되어 있다. Host Device의 Foreground를 담당하는 Host App, Host Device의 Background를 담당하는 Proxy Host App, Guest Device를 담당하는 Guest App이다.
Host App에서는 사용자의 Trigger 요청이 발생하면 Trigger가 발생한 해당 UI에 대한 정보는 AIDL을 통해 Proxy Host App의 Background로 전달이 된다.

    (Source Code)              (Role)
    App              ->         Host
    Host             ->      Proxy Host
    Guest            ->         Guest


Proxy Host App에서는 Guest App과 지속적인 Socket Connection을 하고 있다. 이는 background에서 지속적으로 Connection을 하기 위해 Thread를 통한 Service로 구현되어 있다.
위의 AIDL을 통해 Trigger가 발생한 UI에 대한 data를 받고, Socket Connection을 통해 이를 Guest 디바이스로 전달한다. 

전반적인 System Design은 다음과 같다.

![SystemDesign](https://user-images.githubusercontent.com/77181865/135754420-4d606383-ec8a-4164-ba0f-f72149a1cefc.png)


## 3. Android Interface Definition Language(AIDL)
AIDL은 클라이언트와 서비스가 다른 프로세스일 때, 모두 동일한 프로그래밍 인터페이스를 정의하여 프로세스 간 통신(IPC)을 기반으로 서로 통신을 가능하게 한다.  
Android에서는 일반적으로 한 프로세스가 다른 프로세스의 메모리에 접근할 수 없는데, AIDL을 사용한다면 객체들을 OS가 primitive type으로 해체하고, 해당 boundary에 marshalling한다.
이때 말하는 marshalling이란 한 객체의 메모리에서 표현 방식을 저장하거나 전송하기에 적합한 data type으로 변환하는 과정을 뜻한다. 즉, AIDL을 통해 서로 다른 Application인 Host App과
Proxy Host App이 서로 IPC를 통해 aidl에서 정의한 동일한 함수들을 사용할 수 있다. 본 프로젝트에서 aidl은 다음과 같이 정의되어 있다.

    interface IServiceInterface {
        void isClick();                     //Distribution이 발생했는지 판단
        void isUpdate();                    //Update가 발생했는지 판단
        void setStringText(String text);    //EditText의 Text를 가져오는 함수
        void setSizeOfText(int size);       //EditText의 Text size를 가져오는 함수
        void setFlag(String flag);          //UI의 flag를 가져오는 함수
  
즉, IPC는 Binder로 의해 Foreground의 Host와 Background의 Proxy가 묶임으로서 서로 다른 프로세스끼리 통신을 할 수 있고, AIDL의 method들을 호출함으로써 이는 RPC에 의해 동일한 함수가 구현되어 있는 다른 프로세스에서 호출을 할 수 있는 것이다.


## 4. Socket Programming
Socket Programming은 Proxy Host App과 Guest App이 서로 다른 Device에서 Socket Connection을 통해 통신을 하는 것이 구현되어 있다. Proxy Host App의 Service의 ServerThread에서 Server Socket을 생성하고,  while문을 통해 연결 요청이 올 때까지 대기 했다가 연결 요청이 오면 accept()하며 socket을 지속적으로 생성한다. 마찬가지로 Guest App에서는 ClientThread에서 Socket을 지속적으로 생성한다. 
Proxy Host App의 ```MyService.java``` 와 Guest App의 ```MainActivity.java``` 에서는 Server/Client Thread에서 Socket을 생성하고 이를 Worker Thread에 Message를 만들어 전송하여
Looper & Handler & Message Queue를 통해 Message를 모두 처리한다. 



## 5. Todo
- 현재 구현된 사항으로는 Host -> Guest 일방향 Update가 구현되어 있으나, 추후에 양방향 Interaction이 가능하게끔 구현하기
- 본 프로젝트를 활용하여 Proxy Host App과 Guest App은 유지하고, 사용자의 Foreground를 담당하는 Host App은 Trigger 발생하는 이벤트 및 AIDL interface를 모두 지우고, 
Soot를 통해 Code instrument(AIDL interface class 추가, Trigger 발생 이벤트 추가)를 한다.


## 6. Comment
- 10/5 Comment
    - 현재 구현되어 있는 구조는 Client 측에서 Socket을 지속적으로 생성하고, Server 측에서는 Socket 연결이 들어올 때까지 accept하며 Update나 Distribute가 될 때 매번 Socket 연결이 새로 되는 구조이다. 이러한 구조의 단점은 Guest쪽에서 지속적으로 Socket을 생성하기 때문에 Resource를 많이 차지할 수 있고, UI Distribute나 Update가 많이 발생할 시 매번 Connection이 이루어지는 것이 매우 비효율적이다. 그러므로 Socket은 Server < - > Guest 한번의 연결 시도로 지속적인 연결을 하고,

AIDL에서 받은 UI를 직렬화한 데이터를 다루는 Worker Thread  -> ServerThread -> Client Thread -> Worker Thread(UI distribute or update) 이 구조

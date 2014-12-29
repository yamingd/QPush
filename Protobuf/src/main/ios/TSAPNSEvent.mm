#import "TSAPNSEvent.hh"
#import "pb_message.pb.hh"

@interface TSAPNSEvent ()

@property (nonatomic,strong) NSData *protocolData;

@end


@implementation TSAPNSEvent

-(instancetype) initWithProtocolData:(NSData*) data {
    return [self initWithData:data];
}

-(NSData*) getProtocolData {
    return self.protocolData;
}

-(NSMutableDictionary*) asDict{
    return nil;
}

-(instancetype) initWithData:(NSData*) data {

    if(self = [super init]) {
        // c++
        message::PBAPNSEvent* resp = [self deserialize:data];
        const std::string token = resp->token();
        const std::string appKey = resp->appKey();
        const uint32_t op = resp->op();
        const uint32_t typeId = resp->typeId();

        // c++->objective C
        self.protocolData = data;
        self.token = [PBObjc cppStringToObjc:token];
        self.appKey = [PBObjc cppStringToObjc:appKey];
        self.op = op;
        self.typeId = typeId;
    }
    return self;
}

#pragma mark private

-(const std::string) serializedProtocolBufferAsString {
    message::PBAPNSEvent *message = new message::PBAPNSEvent;
    // objective c->c++
    const std::string token = [PBObjc objcStringToCpp:self.token];
    const std::string appKey = [PBObjc objcStringToCpp:self.appKey];
    const uint32_t op = self.op;
    const uint32_t typeId = self.typeId;

    // c++->protocol buffer
    message->set_token(token);
    message->set_appKey(appKey);
    message->set_op(op);
    message->set_typeId(typeId);

    std::string ps = message->SerializeAsString();
    return ps;
}

#pragma mark private methods
- (message::PBAPNSEvent *)deserialize:(NSData *)data {
    int len = [data length];
    char raw[len];
    message::PBAPNSEvent *resp = new message::PBAPNSEvent;
    [data getBytes:raw length:len];
    resp->ParseFromArray(raw, len);
    return resp;
}

@end
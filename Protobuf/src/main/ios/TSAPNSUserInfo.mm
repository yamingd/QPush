#import "TSAPNSUserInfo.hh"
#import "pb_message.pb.hh"

@interface TSAPNSUserInfo ()

@property (nonatomic,strong) NSData *protocolData;

@end


@implementation TSAPNSUserInfo

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
        message::PBAPNSUserInfo* resp = [self deserialize:data];
        const std::string key = resp->key();
        const std::string value = resp->value();

        // c++->objective C
        self.protocolData = data;
        self.key = [PBObjc cppStringToObjc:key];
        self.value = [PBObjc cppStringToObjc:value];
    }
    return self;
}

#pragma mark private

-(const std::string) serializedProtocolBufferAsString {
    message::PBAPNSUserInfo *message = new message::PBAPNSUserInfo;
    // objective c->c++
    const std::string key = [PBObjc objcStringToCpp:self.key];
    const std::string value = [PBObjc objcStringToCpp:self.value];

    // c++->protocol buffer
    message->set_key(key);
    message->set_value(value);

    std::string ps = message->SerializeAsString();
    return ps;
}

#pragma mark private methods
- (message::PBAPNSUserInfo *)deserialize:(NSData *)data {
    int len = [data length];
    char raw[len];
    message::PBAPNSUserInfo *resp = new message::PBAPNSUserInfo;
    [data getBytes:raw length:len];
    resp->ParseFromArray(raw, len);
    return resp;
}

@end
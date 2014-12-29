#import "TSAPNSBody.hh"
#import "pb_message.pb.hh"

@interface TSAPNSBody ()

@property (nonatomic,strong) NSData *protocolData;

@end


@implementation TSAPNSBody

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
        message::PBAPNSBody* resp = [self deserialize:data];
        const std::string alert = resp->alert();
        const std::string sound = resp->sound();
        const uint32_t badge = resp->badge();

        // c++->objective C
        self.protocolData = data;
        self.alert = [PBObjc cppStringToObjc:alert];
        self.sound = [PBObjc cppStringToObjc:sound];
        self.badge = [PBObjc cppUInt32ToNSNumber:badge];
    }
    return self;
}

#pragma mark private

-(const std::string) serializedProtocolBufferAsString {
    message::PBAPNSBody *message = new message::PBAPNSBody;
    // objective c->c++
    const std::string alert = [PBObjc objcStringToCpp:self.alert];
    const std::string sound = [PBObjc objcStringToCpp:self.sound];
    const uint32_t badge = [PBObjc objcNumberToCppUInt32:self.badge];

    // c++->protocol buffer
    message->set_alert(alert);
    message->set_sound(sound);
    message->set_badge(badge);

    std::string ps = message->SerializeAsString();
    return ps;
}

#pragma mark private methods
- (message::PBAPNSBody *)deserialize:(NSData *)data {
    int len = [data length];
    char raw[len];
    message::PBAPNSBody *resp = new message::PBAPNSBody;
    [data getBytes:raw length:len];
    resp->ParseFromArray(raw, len);
    return resp;
}

@end
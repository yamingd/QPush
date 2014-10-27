#import "TSAPNSMessage.hh"
#import "TSAPNSBody.hh"
#import "TSAPNSUserInfo.hh"
#import "pb_message.pb.hh"

@interface TSAPNSMessage ()

@property (nonatomic,strong) NSData *protocolData;

@end


@implementation TSAPNSMessage

-(instancetype) initWithProtocolData:(NSData*) data {
    return [self initWithData:data];
}

-(NSData*) getProtocolData {
    return self.protocolData;
}


-(instancetype) initWithData:(NSData*) data {

    if(self = [super init]) {
        // c++
        message::PBAPNSMessage* resp = [self deserialize:data];

        const message::TSAPNSBody* body = resp.aps();

        self.aps = [[TSAPNSBody alloc] init];
        self.aps.alert = [self cppStringToObjc:body->alert()];
        self.aps.sound = [self cppStringToObjc:body->sound()];
        self.aps.badge = [self cppUInt32ToNSNumber:body->badge()];

        NSMutableDictionary *uis = [[NSMutableDictionary alloc] init];
        if(resp->has_userInfo()){
            for(int i=0; i < resp.userInfo_size(); i++) {
                const message::TSAPNSUserInfo* ui = resp.userinfo(i);
                const std::string key = ui->key();
                const std::string value = ui->value();

                NSString* key0 = [self cppStringToObjc:key];
                NSString* value0 = [self cppStringToObjc:value];

                [uis setObject:value0 forKey:key0];
            }
        }
        self.userInfo = uis;
        // c++->objective C
        self.protocolData = data;
    }
    return self;
}

#pragma mark private

-(const std::string) serializedProtocolBufferAsString {
    message::PBAPNSMessage *message = new message::PBAPNSMessage;
    // objective c->c++
    const std::string alert = [self objcStringToCpp:self.aps.alert];
    const std::string sound = [self objcStringToCpp:self.aps.sound];
    const uint32_t badge = [self objcNumberToCppUInt32:self.aps.badge];

    // c++->protocol buffer
    message::PBAPNSBody* body = new message::PBAPNSBody;
    body->set_alert(alert);
    body->set_sound(sound);
    body->set_badge(badge);

    message->set_aps(body);

    for(NSString* key in self.userInfo){
        NSString* value = [self.userInfo objectForKey:key];

        const std::string key0 = [self objcStringToCpp:key];
        const std::string value0 = [self objcStringToCpp:value];

        message::PBAPNSUserInfo* ui = new message::PBAPNSUserInfo;
        ui->set_key(key0);
        ui->set_value(value0);

        message->add_userinfo(ui);
    }

    std::string ps = message->SerializeAsString();
    return ps;
}

#pragma mark private methods
- (message::PBAPNSMessage *)deserialize:(NSData *)data {
    int len = [data length];
    char raw[len];
    message::PBAPNSMessage *resp = new message::PBAPNSMessage;
    [data getBytes:raw length:len];
    resp->ParseFromArray(raw, len);
    return resp;
}

@end
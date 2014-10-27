#import <Foundation/Foundation.h>
#import "TSProtocolBufferWrapper.hh"

@interface TSAPNSUserInfo : TSProtocolBufferWrapper

@property (nonatomic,strong) NSString* key;
@property (nonatomic,strong) NSString* value;

@property (readonly,nonatomic,strong) NSData *protocolData;

@end
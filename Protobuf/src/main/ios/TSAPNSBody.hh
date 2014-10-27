#import <Foundation/Foundation.h>
#import "TSProtocolBufferWrapper.hh"

@interface TSAPNSBody : TSProtocolBufferWrapper

@property (nonatomic,strong) NSString* alert;
@property (nonatomic,strong) NSString* sound;
@property (nonatomic,strong) NSNumber* badge;
@property (readonly,nonatomic,strong) NSData *protocolData;

@end
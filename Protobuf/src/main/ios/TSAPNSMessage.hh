#import <Foundation/Foundation.h>
#import "TSProtocolBufferWrapper.hh"

@interface TSAPNSMessage : TSProtocolBufferWrapper

@property (nonatomic,strong) TSAPNSBody* aps;
@property (nonatomic,strong) NSDictionary* userInfo;

@property (readonly,nonatomic,strong) NSData *protocolData;

@end
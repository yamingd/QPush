#import <Foundation/Foundation.h>
#import "PBObjcWrapper.hh"

@interface TSAPNSMessage : NSObject<PBObjcWrapper>

@property (nonatomic,strong) TSAPNSBody* aps;
@property (nonatomic,strong) NSDictionary* userInfo;

@property (readonly,nonatomic,strong) NSData *protocolData;

@end